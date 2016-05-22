package js

import (
	"fmt"
)

// Parser is a table-driven LALR parser for js.
type Parser struct {
	err ErrorHandler
	listener Listener

	stack []node
	lexer *Lexer
	next  Token
}

type node struct {
	symbol    int32
	state     int32
	value     interface{}
	offset    int
	endoffset int
}

func (p *Parser) Init(err ErrorHandler, l Listener) {
	p.err = err
	p.listener = l
}

const (
	startStackSize = 512
	debugSyntax    = false
)

func (p *Parser) Parse(lexer *Lexer) (bool, interface{}) {
	return p.parse(0, 2693, lexer)
}

func (p *Parser) parse(start, end int32, lexer *Lexer) (bool, interface{}) {
	if cap(p.stack) < startStackSize {
		p.stack = make([]node, 0, startStackSize)
	}
	state := start

	p.stack = append(p.stack[:0], node{state: state})
	p.lexer = lexer
	p.next = lexer.Next()

	for state != end {
		action := p.action(state)

		if action >= 0 {
			// Reduce.
			rule := action
			ln := int(tmRuleLen[rule])

			var node node
			node.symbol = tmRuleSymbol[rule]
			if debugSyntax {
				fmt.Printf("reduce to: %v\n", Symbol(node.symbol))
			}
			if ln == 0 {
				node.offset, _ = lexer.Pos()
				node.endoffset = node.offset
			} else {
				node.offset = p.stack[len(p.stack)-ln].offset
				node.endoffset = p.stack[len(p.stack)-1].endoffset
			}
			p.applyRule(rule, &node, p.stack[len(p.stack)-ln:])
			p.stack = p.stack[:len(p.stack)-ln]
			state = p.gotoState(p.stack[len(p.stack)-1].state, node.symbol)
			node.state = state
			p.stack = append(p.stack, node)

		} else if action == -1 {
			// Shift.
			if p.next == UNAVAILABLE {
				p.next = lexer.Next()
			}
			state = p.gotoState(state, int32(p.next))
			s, e := lexer.Pos()
			p.stack = append(p.stack, node{
				symbol:    int32(p.next),
				state:     state,
				value:     lexer.Value(),
				offset:    s,
				endoffset: e,
			})
			if debugSyntax {
				fmt.Printf("shift: %v (%s)\n", p.next, lexer.Text())
			}
			if state != -1 && p.next != EOI {
				p.next = UNAVAILABLE
			}
		}

		if action == -2 || state == -1 {
			break
		}
	}

	if state != end {
		offset, endoffset := lexer.Pos()
		line := lexer.Line()
		p.err(line, offset, endoffset-offset, "syntax error")
		return false, nil
	}

	return true, p.stack[len(p.stack)-2].value
}

func (p *Parser) action(state int32) int32 {
	a := tmAction[state]
	if a < -2 {
		// Lookahead is needed.
		if p.next == UNAVAILABLE {
			p.next = p.lexer.Next()
		}
		a = -a - 3
		for ; tmLalr[a] >= 0; a += 2 {
			if tmLalr[a] == int32(p.next) {
				break
			}
		}
		return tmLalr[a+1]
	}
	return a
}

func (p *Parser) gotoState(state, symbol int32) int32 {
	min := tmGoto[symbol]
	max := tmGoto[symbol+1] - 1

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
	return -1
}

func (p *Parser) applyRule(rule int32, node *node, rhs []node) {
	nt := ruleNodeType[rule]
	if nt == 0 {
		return
	}
	p.listener.Node(nt, node.offset, node.endoffset)
}
