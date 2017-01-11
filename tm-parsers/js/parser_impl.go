package js

import (
	"fmt"
)

// ErrorHandler is called every time a lexer or parser is unable to process
// some part of the input.
type ErrorHandler func(line, offset, len int, msg string)

// Parser is a table-driven LALR parser for Javascript.
type Parser struct {
	err      ErrorHandler
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

func (p *Parser) Init(err ErrorHandler, l Listener) {
	p.err = err
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

func (p *Parser) parse(start, end int16, lexer *Lexer) bool {
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
			if debugSyntax {
				fmt.Printf("reduce to: %v\n", Symbol(entry.sym.symbol))
			}
			if ln == 0 {
				entry.sym.offset = p.next.offset
				entry.sym.endoffset = p.next.offset
			} else {
				entry.sym.offset = p.stack[len(p.stack)-ln].sym.offset
				entry.sym.endoffset = p.stack[len(p.stack)-1].sym.endoffset
			}
			p.applyRule(rule, &entry, p.stack[len(p.stack)-ln:])
			p.stack = p.stack[:len(p.stack)-ln]
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
				fmt.Printf("shift: %v (%s)\n", Token(p.next.symbol), lexer.Text())
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
				line := lexer.Line()
				p.err(line, offset, endoffset-offset, "syntax error")
			}
			skipToken := recovering >= 3
			if !p.recover(skipToken) {
				return false
			}
			p.healthy = true
			state = p.stack[len(p.stack)-1].state
			recovering = 4
		}
	}

	return true
}

func canRecoverOn(symbol int32) bool {
	for _, v := range afterErr {
		if v == symbol {
			return true
		}
	}
	return false
}

func (p *Parser) recover(skipToken bool) bool {
	if p.next.symbol == eoiToken {
		return false
	}
	e := p.next.offset
	s := e
	for len(p.stack) > 0 && gotoState(p.stack[len(p.stack)-1].state, errSymbol) == -1 {
		s = p.stack[len(p.stack)-1].sym.offset
		p.stack = p.stack[:len(p.stack)-1]
	}
	if len(p.stack) > 0 {
		state := gotoState(p.stack[len(p.stack)-1].state, errSymbol)
		p.stack = append(p.stack, stackEntry{
			sym:   symbol{errSymbol, s, e},
			state: state,
		})
		if p.next.symbol == noToken || skipToken {
			p.fetchNext()
		}
		for p.next.symbol != eoiToken && !canRecoverOn(p.next.symbol) {
			p.fetchNext()
		}
		p.stack[len(p.stack)-1].sym.endoffset = p.next.offset
		return true
	}
	return false
}

func (p *Parser) reportIgnoredTokens() {
	for _, c := range p.ignoredTokens {
		var t NodeType
		switch Token(c.symbol) {
		case MULTILINECOMMENT:
			t = MultiLineComment
		case SINGLELINECOMMENT:
			t = SingleLineComment
		case INVALID_TOKEN:
			t = InvalidToken
		default:
			continue
		}
		p.listener(t, c.offset, c.endoffset)
	}
	p.ignoredTokens = p.ignoredTokens[:0]
}

// reduceAll simulates all pending reductions and return true if the parser
// can consume the next token. This function also returns the state of the
// parser after the reductions have been applied.
func (p *Parser) reduceAll() (state int16, success bool) {
	if p.next.symbol == noToken {
		panic("a valid next token is expected")
	}

	size := len(p.stack)
	state = p.stack[size-1].state

	var stack2alloc [4]int16
	stack2 := stack2alloc[:0]

	for state != p.endState {
		action := tmAction[state]
		if action < -2 {
			action = lalr(action, p.next.symbol)
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
					state = p.stack[size-1].state
					stack2 = stack2alloc[:0]
				}
			}
			state = gotoState(state, symbol)
			stack2 = append(stack2, state)
		} else {
			success = (action == -1 && gotoState(state, p.next.symbol) >= 0)
			return
		}
	}
	success = true
	return
}

// insertSC inserts and reports a semicolon, unless there is a overriding rule
// forbidding insertion in this particular location.
func (p *Parser) insertSC(state int16, offset int) {
	stateAfterSC := gotoState(state, int32(SEMICOLON))
	if stateAfterSC == emptyStatementState || forSCStates[int(stateAfterSC)] {
		// ".. a semicolon is never inserted automatically if the semicolon would
		// then be parsed as an empty statement or if that semicolon would become
		// one of the two semicolons in the header of a for statement."
		return
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
		}

		if restricted {
			p.insertSC(p.stack[len(p.stack)-1].state, lastEnd)
			return
		}
	}

	// Simulate all pending reductions and check if the current next token
	// will be accepted by the parser.
	state, success := p.reduceAll()

	if newLine && success && (token == PLUSPLUS || token == MINUSMINUS) {
		if noLineBreakStates[int(state)] {
			p.insertSC(state, lastEnd)
			return
		}
	}

	// When recovering from a syntax error, we cannot rely on the current state
	// of the stack and assume that the next token won't be accepted by the
	// parser, so in general we insert more semicolons than needed. This is
	// exactly what we want.
	if success && p.healthy {
		return
	}

	if newLine || token == RBRACE || token == EOI {
		p.insertSC(state, lastEnd)
		return
	}

	if lastToken == RPAREN && doWhileStates[int(gotoState(state, int32(SEMICOLON)))] {
		p.insertSC(state, lastEnd)
		return
	}
}

func lalr(action, next int32) int32 {
	a := -action - 3
	for ; tmLalr[a] >= 0; a += 2 {
		if tmLalr[a] == next {
			break
		}
	}
	return tmLalr[a+1]
}

func gotoState(state int16, symbol int32) int16 {
	min := tmGoto[symbol]
	max := tmGoto[symbol+1] - 1

	if max-min < 16 {
		for i := min; i <= max; i++ {
			if tmFrom[i] == state {
				return tmTo[i]
			}
		}
	} else {
		for min <= max {
			e := (min + max) >> 1
			i := tmFrom[e]
			if i == state {
				return tmTo[e]
			} else if i < state {
				min = e + 1
			} else {
				max = e - 1
			}
		}
	}
	return -1
}
