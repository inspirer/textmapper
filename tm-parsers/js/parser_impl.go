package js

import (
	"context"
	"fmt"
)

// Parser is a table-driven LALR parser for Javascript.
type Parser struct {
	eh       ErrorHandler
	listener Listener

	next      symbol
	afterNext symbol
	pending   []symbol
	healthy   bool

	lastToken Token
	lastLine  int
	endState  int16
}

type symbol struct {
	symbol    int32
	offset    int
	endoffset int
}

type stackEntry struct {
	sym   symbol
	state int16
}

func (p *Parser) Init(eh ErrorHandler, l Listener) {
	p.eh = eh
	p.listener = l
	if cap(p.pending) < startTokenBufferSize {
		p.pending = make([]symbol, 0, startTokenBufferSize)
	}
	p.lastToken = UNAVAILABLE
	p.afterNext.symbol = noToken
}

const (
	startStackSize       = 256
	startTokenBufferSize = 16
	noToken              = int32(UNAVAILABLE)
	eoiToken             = int32(EOI)
	debugSyntax          = false
)

type session struct {
	shiftCounter int32
	cache        map[uint64]bool
}

func (p *Parser) parse(ctx context.Context, start, end int16, lexer *Lexer) error {
	p.pending = p.pending[:0]
	var s session
	s.cache = make(map[uint64]bool)

	state := start
	p.endState = end
	var lastErr SyntaxError
	recovering := 0
	p.healthy = true

	var alloc [startStackSize]stackEntry
	stack := append(alloc[:0], stackEntry{state: state})
	p.fetchNext(lexer, stack)

	for state != end {
		action := tmAction[state]
		if action < -2 {
			// Lookahead is needed.
			if p.next.symbol == noToken {
				p.fetchNext(lexer, stack)
			}
			action = lalr(action, p.next.symbol)
		}

		if action >= 0 {
			// Reduce.
			rule := action
			ln := int(tmRuleLen[rule])

			var entry stackEntry
			entry.sym.symbol = tmRuleSymbol[rule]
			rhs := stack[len(stack)-ln:]
			stack = stack[:len(stack)-ln]
			for ln > 0 && rhs[ln-1].sym.offset == rhs[ln-1].sym.endoffset {
				ln--
			}
			if ln == 0 {
				if p.next.symbol == noToken {
					p.fetchNext(lexer, stack)
				}
				entry.sym.offset, entry.sym.endoffset = p.next.offset, p.next.offset
			} else {
				entry.sym.offset = rhs[0].sym.offset
				entry.sym.endoffset = rhs[ln-1].sym.endoffset
			}
			p.applyRule(ctx, rule, &entry, rhs, lexer, &s)
			if debugSyntax {
				fmt.Printf("reduced to: %v\n", symbolName(entry.sym.symbol))
			}
			state = gotoState(stack[len(stack)-1].state, entry.sym.symbol)
			entry.state = state
			stack = append(stack, entry)

		} else if action == -1 {
			if s.shiftCounter++; s.shiftCounter&0x1ff == 0 {
				// Note: checking for context cancellation is expensive so we do it from time to time.
				select {
				case <-ctx.Done():
					return ctx.Err()
				default:
				}
			}

			// Shift.
			if p.next.symbol == noToken {
				p.fetchNext(lexer, stack)
			}
			state = gotoState(state, p.next.symbol)
			if state >= 0 {
				stack = append(stack, stackEntry{
					sym:   p.next,
					state: state,
				})
				if debugSyntax {
					fmt.Printf("shift: %v (%s)\n", symbolName(p.next.symbol), lexer.Text())
				}
				if len(p.pending) > 0 {
					for _, tok := range p.pending {
						p.reportIgnoredToken(tok)
					}
					p.pending = p.pending[:0]
				}
				if p.next.symbol != eoiToken {
					switch Token(p.next.symbol) {
					case NOSUBSTITUTIONTEMPLATE:
						p.listener(NoSubstitutionTemplate, p.next.offset, p.next.endoffset)
					case TEMPLATEHEAD:
						p.listener(TemplateHead, p.next.offset, p.next.endoffset)
					case TEMPLATEMIDDLE:
						p.listener(TemplateMiddle, p.next.offset, p.next.endoffset)
					case TEMPLATETAIL:
						p.listener(TemplateTail, p.next.offset, p.next.endoffset)
					}
					p.next.symbol = noToken
				}
				if recovering > 0 {
					recovering--
				}
			}
		}

		if action == -2 || state == -1 {
			p.healthy = false
			if recovering == 0 {
				if p.next.symbol == noToken {
					p.fetchNext(lexer, stack)
				}
				if len(p.pending) > 0 {
					for _, tok := range p.pending {
						p.reportIgnoredToken(tok)
					}
					p.pending = p.pending[:0]
				}
				lastErr = SyntaxError{
					Line:      lexer.Line(),
					Offset:    p.next.offset,
					Endoffset: p.next.endoffset,
				}
				if !p.eh(lastErr) {
					return lastErr
				}
			}
			stack = p.recoverFromError(lexer, stack)
			if stack == nil {
				return lastErr
			}
			p.healthy = true
			state = stack[len(stack)-1].state
			recovering = 4
		}
	}

	return nil
}

func (p *Parser) recoverFromError(lexer *Lexer, stack []stackEntry) []stackEntry {
	var recoverSyms [1 + NumTokens/8]uint8
	var recoverPos []int

	if debugSyntax {
		fmt.Printf("broke at %v\n", symbolName(p.next.symbol))
	}
	for size := len(stack); size > 0; size-- {
		if gotoState(stack[size-1].state, errSymbol) == -1 {
			continue
		}
		recoverPos = append(recoverPos, size)
		if recoveryScopeStates[int(stack[size-1].state)] {
			break
		}
	}
	if len(recoverPos) == 0 {
		return nil
	}

	for _, v := range afterErr {
		recoverSyms[v/8] |= 1 << uint32(v%8)
	}
	canRecover := func(symbol int32) bool {
		return recoverSyms[symbol/8]&(1<<uint32(symbol%8)) != 0
	}
	if p.next.symbol == noToken {
		p.fetchNext(lexer, stack)
	}
	// By default, insert 'error' in front of the next token.
	s := p.next.offset
	e := s
	for {
		if endoffset := p.skipBrokenCode(lexer, stack, canRecover); endoffset > e {
			e = endoffset
		}

		var matchingPos int
		if debugSyntax {
			fmt.Printf("trying to recover on %v\n", symbolName(p.next.symbol))
		}
		for _, pos := range recoverPos {
			errState := gotoState(stack[pos-1].state, errSymbol)
			if p.willShift(pos, gotoState(stack[pos-1].state, errSymbol), p.next.symbol, stack) {
				matchingPos = pos
				break
			}
			// Semicolon insertion is not reliable on broken input, try to look behind the semicolon.
			if p.afterNext.symbol != noToken && p.willShift(pos, errState, p.afterNext.symbol, stack) {
				if len(p.pending) > 0 {
					for _, tok := range p.pending {
						p.reportIgnoredToken(tok)
					}
					p.pending = p.pending[:0]
				}
				p.fetchNext(lexer, stack)
				matchingPos = pos
				break
			}
		}
		if matchingPos == 0 {
			if p.next.symbol == eoiToken {
				return nil
			}
			recoverSyms[p.next.symbol/8] &^= 1 << uint32(p.next.symbol%8)
			continue
		}

		if matchingPos < len(stack) {
			if s == e {
				// Avoid producing syntax problems covering trailing whitespace.
				e = stack[len(stack)-1].sym.endoffset
			}
			s = stack[matchingPos].sym.offset
		} else if s == e && len(p.pending) > 0 {
			for _, tok := range p.pending {
				p.reportIgnoredToken(tok)
			}
			p.pending = p.pending[:0]
		}
		if debugSyntax {
			for i := len(stack) - 1; i >= matchingPos; i-- {
				fmt.Printf("dropped from stack: %v\n", symbolName(stack[i].sym.symbol))
			}
			fmt.Println("recovered")
		}
		stack = append(stack[:matchingPos], stackEntry{
			sym:   symbol{errSymbol, s, e},
			state: gotoState(stack[matchingPos-1].state, errSymbol),
		})
		return stack
	}
}

func lookaheadNext(lexer *Lexer, endState int16, stack []stackEntry) int32 {
restart:
	tok := lexer.Next()
	switch tok {
	case MULTILINECOMMENT, SINGLELINECOMMENT, INVALID_TOKEN:
		goto restart
	case GTGT, GTGTGT:
		if _, success := reduceAll(int32(tok), endState, stack); !success {
			tok = GT
			lexer.offset = lexer.tokenOffset + 1
			lexer.scanOffset = lexer.offset + 1
			lexer.ch = '>'
			lexer.token = tok
		}
	}
	return int32(tok)
}

// reduceAll simulates all pending reductions and return true if the parser
// can consume the next token. This function also returns the state of the
// parser after the reductions have been applied.
func reduceAll(next int32, endState int16, stack []stackEntry) (state int16, success bool) {
	if next == noToken {
		panic("a valid next token is expected")
	}

	size := len(stack)
	state = stack[size-1].state
	if state < 0 {
		return 0, false
	}

	var stack2alloc [4]int16
	stack2 := stack2alloc[:0]

	for state != endState {
		action := tmAction[state]
		if action < -2 {
			action = lalr(action, next)
		}

		if action >= 0 {
			// Reduce.
			rule := action
			ln := int(tmRuleLen[rule])
			symbol := tmRuleSymbol[rule]

			if ln > 0 {
				if ln < len(stack2) {
					state = stack2[len(stack2)-ln-1]
					stack2 = stack2[:len(stack2)-ln]
				} else {
					size -= ln - len(stack2)
					state = stack[size-1].state
					stack2 = stack2alloc[:0]
				}
			}
			state = gotoState(state, symbol)
			stack2 = append(stack2, state)
		} else {
			success = (action == -1 && gotoState(state, next) >= 0)
			return
		}
	}
	success = true
	return
}

// insertSC inserts and reports a semicolon, unless there is a overriding rule
// forbidding insertion in this particular location.
func (p *Parser) insertSC(state int16, offset int) {
	if p.healthy {
		stateAfterSC := gotoState(state, int32(SEMICOLON))
		if stateAfterSC == emptyStatementState || forSCStates[int(stateAfterSC)] {
			// ".. a semicolon is never inserted automatically if the semicolon would
			// then be parsed as an empty statement or if that semicolon would become
			// one of the two semicolons in the header of a for statement."
			return
		}
	}

	p.afterNext = p.next
	p.next = symbol{int32(SEMICOLON), offset, offset}
	p.listener(InsertedSemicolon, offset, offset)
}

// fetchNext fetches the next token from the lexer and puts it into "p.next".
// This function also takes care of semicolons by implementing the "Automatic
// Semicolon Insertion" rules.
func (p *Parser) fetchNext(lexer *Lexer, stack []stackEntry) {
	if p.afterNext.symbol != noToken {
		p.next = p.afterNext
		p.afterNext.symbol = noToken
		return
	}

	lastToken := p.lastToken
	lastEnd := p.next.endoffset
restart:
	token := lexer.Next()
	switch token {
	case MULTILINECOMMENT, SINGLELINECOMMENT, INVALID_TOKEN:
		s, e := lexer.Pos()
		tok := symbol{int32(token), s, e}
		p.pending = append(p.pending, tok)
		goto restart
	case GTGT, GTGTGT:
		if _, success := reduceAll(int32(token), p.endState, stack); !success {
			token = GT
			lexer.offset = lexer.tokenOffset + 1
			lexer.scanOffset = lexer.offset + 1
			lexer.ch = '>'
			lexer.token = token
		}
	}
	p.lastToken = token
	p.next.symbol = int32(token)
	p.next.offset, p.next.endoffset = lexer.Pos()
	line := lexer.Line()

	newLine := line != p.lastLine
	p.lastLine = line

	if !(newLine || token == RBRACE || token == EOI || lastToken == RPAREN) || lastToken == SEMICOLON {
		return
	}

	if !p.healthy {
		// When recovering from a syntax error, we cannot rely on the current state
		// of the stack and assume that the next token won't be accepted by the
		// parser, so in general we insert more semicolons than needed. This is
		// exactly what we want.
		if newLine || token == RBRACE || token == EOI {
			p.insertSC(-1 /* no state */, lastEnd)
		}
		return
	}

	// We might need to insert a semicolon.
	// See 12.9.1 Rules of Automatic Semicolon Insertion
	if newLine {
		// All but one of the restricted productions can be detected by looking
		// at the last and current tokens.
		restricted := token == ASSIGNGT
		switch lastToken {
		case CONTINUE, BREAK, RETURN, THROW:
			restricted = true
		case YIELD:
			// No reduce actions are expected, so we can take a shortcut and check
			// the current state.
			restricted = afterYieldStates[int(stack[len(stack)-1].state)]
		case ASYNC:
			// No reduce actions are expected, so we can take a shortcut and check
			// the current state.
			restricted = afterAsyncStates[int(stack[len(stack)-1].state)]
		case STRINGLITERAL:
			// Assert clauses should appear on the same line.
			restricted = token == ASSERT && noLineBreakStates[int(stack[len(stack)-1].state)]
		}

		if restricted {
			p.insertSC(stack[len(stack)-1].state, lastEnd)
			return
		}
	}

	// Simulate all pending reductions and check if the current next token
	// will be accepted by the parser.
	state, success := reduceAll(p.next.symbol, p.endState, stack)

	if newLine && success && (token == PLUSPLUS || token == MINUSMINUS || token == AS || token == EXCL) {
		if noLineBreakStates[int(state)] {
			p.insertSC(state, lastEnd)
			return
		}
	}

	if success {
		return
	}

	if token == RBRACE {
		// Not all closing braces require a semicolon. Double checking.
		if _, success = reduceAll(int32(SEMICOLON), p.endState, stack); success {
			p.insertSC(state, lastEnd)
		}
		return
	}

	if newLine || token == EOI {
		p.insertSC(state, lastEnd)
		return
	}

	if lastToken == RPAREN && doWhileStates[int(gotoState(state, int32(SEMICOLON)))] {
		p.insertSC(state, lastEnd)
		return
	}
	return
}
