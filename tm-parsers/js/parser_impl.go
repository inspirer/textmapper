package js

import (
	"fmt"
)

// Parser is a table-driven LALR parser for Javascript.
type Parser struct {
	eh       ErrorHandler
	listener Listener

	stack         []stackEntry
	lexer         *Lexer
	next          symbol
	afterNext     symbol
	ignoredTokens []symbol // to be reported with the next shift
	healthy       bool

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
	p.lastToken = UNAVAILABLE
	p.afterNext.symbol = -1
}

const (
	startStackSize       = 512
	startTokenBufferSize = 16
	noToken              = int32(UNAVAILABLE)
	eoiToken             = int32(EOI)
	debugSyntax          = false
)

func (p *Parser) parse(start, end int16, lexer *Lexer) error {
	if cap(p.stack) < startStackSize {
		p.stack = make([]stackEntry, 0, startStackSize)
	}
	if cap(p.ignoredTokens) < startTokenBufferSize {
		p.ignoredTokens = make([]symbol, 0, startTokenBufferSize)
	} else {
		p.ignoredTokens = p.ignoredTokens[:0]
	}
	state := start
	p.endState = end
	var lastErr SyntaxError
	recovering := 0
	p.healthy = true

	p.stack = append(p.stack[:0], stackEntry{state: state})
	p.lexer = lexer
	p.fetchNext()

	for state != end {
		action := tmAction[state]
		if action < -2 {
			// Lookahead is needed.
			if p.next.symbol == noToken {
				p.fetchNext()
			}
			action = lalr(action, p.next.symbol)
		}

		if action >= 0 {
			// Reduce.
			rule := action
			ln := int(tmRuleLen[rule])

			var entry stackEntry
			entry.sym.symbol = tmRuleSymbol[rule]
			rhs := p.stack[len(p.stack)-ln:]
			p.stack = p.stack[:len(p.stack)-ln]
			for ln > 0 && rhs[ln-1].sym.offset == rhs[ln-1].sym.endoffset {
				ln--
			}
			if ln == 0 {
				entry.sym.offset, _ = lexer.Pos()
				entry.sym.endoffset = entry.sym.offset
			} else {
				entry.sym.offset = rhs[0].sym.offset
				entry.sym.endoffset = rhs[ln-1].sym.endoffset
			}
			p.applyRule(rule, &entry, rhs)
			if debugSyntax {
				fmt.Printf("reduced to: %v\n", Symbol(entry.sym.symbol))
			}
			state = gotoState(p.stack[len(p.stack)-1].state, entry.sym.symbol)
			entry.state = state
			p.stack = append(p.stack, entry)

		} else if action == -1 {
			// Shift.
			if p.next.symbol == noToken {
				p.fetchNext()
			}
			state = gotoState(state, p.next.symbol)
			p.stack = append(p.stack, stackEntry{
				sym:   p.next,
				state: state,
			})
			if debugSyntax {
				fmt.Printf("shift: %v (%s)\n", Symbol(p.next.symbol), lexer.Text())
			}
			if len(p.ignoredTokens) > 0 {
				p.reportIgnoredTokens()
			}
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
			if state != -1 && p.next.symbol != eoiToken {
				p.next.symbol = noToken
			}
			if recovering > 0 {
				recovering--
			}
		}

		if action == -2 || state == -1 {
			p.healthy = false
			if recovering == 0 {
				offset, endoffset := lexer.Pos()
				lastErr = SyntaxError{
					Line:      lexer.Line(),
					Offset:    offset,
					Endoffset: endoffset,
				}
				if !p.eh(lastErr) {
					return lastErr
				}
			}
			if !p.recoverFromError() {
				if len(p.ignoredTokens) > 0 {
					p.reportIgnoredTokens()
				}
				return lastErr
			}
			p.healthy = true
			state = p.stack[len(p.stack)-1].state
			recovering = 4
		}
	}

	return nil
}

func (p *Parser) recoverFromError() bool {
	var seen map[int32]bool
	var recoverPos []int

	for size := len(p.stack); size > 0; size-- {
		if gotoState(p.stack[size-1].state, errSymbol) == -1 {
			continue
		}
		recoverPos = append(recoverPos, size)
	}
	if len(recoverPos) == 0 {
		return false
	}

	if p.next.symbol == noToken {
		p.fetchNext()
	}
	s := p.next.offset
	e := s
	for {
		for p.next.symbol != eoiToken && (!canRecoverOn(p.next.symbol) || seen[p.next.symbol]) {
			e = p.next.endoffset
			p.fetchNext()
		}

		var matchingPos int
		for _, pos := range recoverPos {
			errState := gotoState(p.stack[pos-1].state, errSymbol)
			if p.willShift(pos, gotoState(p.stack[pos-1].state, errSymbol), p.next.symbol) {
				matchingPos = pos
				break
			}
			// Semicolon insertion is not reliable on broken input, try to look behind the semicolon.
			if p.afterNext.symbol != -1 && p.willShift(pos, errState, p.afterNext.symbol) {
				p.fetchNext()
				matchingPos = pos
				break
			}
		}
		if matchingPos == 0 {
			if seen == nil {
				seen = make(map[int32]bool)
			}
			if p.next.symbol == eoiToken {
				return false
			}
			seen[p.next.symbol] = true
			continue
		}

		if matchingPos < len(p.stack) {
			s = p.stack[matchingPos].sym.offset
		}
		p.stack = append(p.stack[:matchingPos], stackEntry{
			sym:   symbol{errSymbol, s, e},
			state: gotoState(p.stack[matchingPos-1].state, errSymbol),
		})
		return true
	}
	return false
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
func (p *Parser) fetchNext() {
	if p.afterNext.symbol != -1 {
		p.next = p.afterNext
		p.afterNext.symbol = -1
		return
	}

	lastToken := p.lastToken
	lastEnd := p.next.endoffset
restart:
	token := p.lexer.Next()
	switch token {
	case MULTILINECOMMENT, SINGLELINECOMMENT, INVALID_TOKEN:
		s, e := p.lexer.Pos()
		p.ignoredTokens = append(p.ignoredTokens, symbol{int32(token), s, e})
		goto restart
	case GTGT, GTGTGT:
		if _, success := reduceAll(int32(token), p.endState, p.stack); !success {
			token = GT
			p.lexer.offset = p.lexer.tokenOffset + 1
			p.lexer.scanOffset = p.lexer.offset + 1
			p.lexer.ch = '>'
			p.lexer.token = token
		}
	}
	p.lastToken = token
	p.next.symbol = int32(token)
	p.next.offset, p.next.endoffset = p.lexer.Pos()
	line := p.lexer.Line()

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
	// See 11.9.1 Rules of Automatic Semicolon Insertion
	if newLine {
		// All but one of the restricted productions can be detected by looking
		// at the last and current tokens.
		restricted := (token == ASSIGNGT)
		switch lastToken {
		case CONTINUE, BREAK, RETURN, THROW:
			restricted = true
		case YIELD:
			// No reduce actions are expected, so we can take a shortcut and check
			// the current state.
			restricted = afterYieldStates[int(p.stack[len(p.stack)-1].state)]
		case ASYNC:
			// No reduce actions are expected, so we can take a shortcut and check
			// the current state.
			restricted = afterAsyncStates[int(p.stack[len(p.stack)-1].state)]
		}

		if restricted {
			p.insertSC(p.stack[len(p.stack)-1].state, lastEnd)
			return
		}
	}

	// Simulate all pending reductions and check if the current next token
	// will be accepted by the parser.
	state, success := reduceAll(p.next.symbol, p.endState, p.stack)

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
		if _, success = reduceAll(int32(SEMICOLON), p.endState, p.stack); success {
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
}
