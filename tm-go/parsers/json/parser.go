package json

import (
	"fmt"
)

// Parser is a table-driven LALR parser for json.
type Parser struct {
	err ErrorHandler

	stack []node
	lexer *Lexer
	next Token
}

type node struct {
	symbol    int32
	state     int32
	value     interface{}
	offset    int
	endoffset int
}

func (p *Parser) Init(err ErrorHandler) {
	p.err = err
}

const (
	startStackSize = 512
	debugSyntax    = false
)

func (p *Parser) Parse(lexer *Lexer) (bool, interface{}) {
	return p.parse(0, 27, lexer)
}

func (p *Parser) parse(start, end int32, lexer *Lexer) (bool, interface{}) {
	if cap(p.stack) < startStackSize {
		p.stack = make([]node, 0, startStackSize)
	}
	state := start
	recovering := 0

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
				fmt.Printf("reduce to: %v\n", tmSymbolNames[node.symbol-int32(terminalEnd)])
			}
			if ln == 0 {
				node.offset, _ = lexer.Pos()
				node.endoffset = node.offset
			} else {
				node.offset = p.stack[len(p.stack)-ln].offset
				node.endoffset = p.stack[len(p.stack)-1].endoffset
			}
			p.applyRule(&node, rule, tmRuleLen[rule])
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
				offset:    s,
				endoffset: e,
			})
			if debugSyntax {
				fmt.Printf("shift: %v (%s)\n", p.next, lexer.Text())
			}
			if state != -1 && p.next != EOI {
				p.next = UNAVAILABLE
			}
			if recovering > 0 {
				recovering--
			}
		}

		if action == -2 || state == -1 {
			if p.recover() {
				state = p.stack[len(p.stack)-1].state
				if recovering == 0 {
					offset, endoffset := lexer.Pos()
					line := lexer.Line()
					p.err(line, offset, endoffset - offset, "syntax error")
				}
				if recovering >= 3 {
					p.next = lexer.Next()
				}
				recovering = 4
				continue
			}
			if len(p.stack) == 0 {
				state = start
				p.stack = append(p.stack, node{state: state})
			}
			break
		}
	}

	if state != end {
		if recovering > 0 {
			return false, nil
		}
		offset, endoffset := lexer.Pos()
		line := lexer.Line()
		p.err(line, offset, endoffset - offset, "syntax error")
		return false, nil
	}

	return true, p.stack[len(p.stack)-1].value
}

func (p *Parser) recover() bool {
	if p.next == UNAVAILABLE {
		p.next = p.lexer.Next()
	}
	if p.next == EOI {
		return false
	}
	e, _ := p.lexer.Pos()
	s := e
	for len(p.stack) > 0 && p.gotoState(p.stack[len(p.stack)-1].state, 13) == -1 {
	    // TODO cleanup
		p.stack = p.stack[:len(p.stack)-1]
		s = p.stack[len(p.stack)-1].offset
	}
	if len(p.stack) > 0 {
	    state := p.gotoState(p.stack[len(p.stack)-1].state, 13)
		p.stack = append(p.stack, node{
			symbol:    13,
			state:     state,
			offset:    s,
			endoffset: e,
		})
		return true
	}
	return false
}

func (p *Parser) action(state int32) int32 {
	a := tmAction[state]
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

func (p* Parser) applyRule(node *node, rule, ruleLen int32) {
}
