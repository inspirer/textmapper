package json

import (
	"fmt"
)

// Parser is a table-driven LALR parser for json.
type Parser struct {
	err      ErrorHandler
	listener Listener

	stack []node
	lexer *Lexer
	next  symbol
}

type symbol struct {
	symbol    int32
	offset    int
	endoffset int
}

type node struct {
	sym   symbol
	state int32
	value int
}

func (p *Parser) Init(err ErrorHandler, l Listener) {
	p.err = err
	p.listener = l
}

const (
	startStackSize = 512
	noToken        = int32(UNAVAILABLE)
	eoiToken       = int32(EOI)
	debugSyntax    = false
)

func (p *Parser) Parse(lexer *Lexer) (bool, int) {
	return p.parse(0, 36, lexer)
}

func (p *Parser) parse(start, end int32, lexer *Lexer) (bool, int) {
	if cap(p.stack) < startStackSize {
		p.stack = make([]node, 0, startStackSize)
	}
	state := start
	recovering := 0

	p.stack = append(p.stack[:0], node{state: state})
	p.lexer = lexer
	p.next.symbol = int32(lexer.Next())
	p.next.offset, p.next.endoffset = lexer.Pos()

	for state != end {
		action := p.action(state)

		if action >= 0 {
			// Reduce.
			rule := action
			ln := int(tmRuleLen[rule])

			var node node
			node.sym.symbol = tmRuleSymbol[rule]
			if debugSyntax {
				fmt.Printf("reduce to: %v\n", Symbol(node.sym.symbol))
			}
			if ln == 0 {
				node.sym.offset, _ = lexer.Pos()
				node.sym.endoffset = node.sym.offset
			} else {
				node.sym.offset = p.stack[len(p.stack)-ln].sym.offset
				node.sym.endoffset = p.stack[len(p.stack)-1].sym.endoffset
			}
			p.applyRule(rule, &node, p.stack[len(p.stack)-ln:])
			p.stack = p.stack[:len(p.stack)-ln]
			state = p.gotoState(p.stack[len(p.stack)-1].state, node.sym.symbol)
			node.state = state
			p.stack = append(p.stack, node)

		} else if action == -1 {
			// Shift.
			if p.next.symbol == noToken {
				p.next.symbol = int32(lexer.Next())
				p.next.offset, p.next.endoffset = lexer.Pos()
			}
			state = p.gotoState(state, p.next.symbol)
			p.stack = append(p.stack, node{
				sym:   p.next,
				state: state,
			})
			if debugSyntax {
				fmt.Printf("shift: %v (%s)\n", Symbol(p.next.symbol), lexer.Text())
			}
			if state != -1 && p.next.symbol != eoiToken {
				p.next.symbol = noToken
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
					p.err(line, offset, endoffset-offset, "syntax error")
				}
				if recovering >= 3 {
					p.next.symbol = int32(p.lexer.Next())
					p.next.offset, p.next.endoffset = lexer.Pos()
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
			return false, 0
		}
		offset, endoffset := lexer.Pos()
		line := lexer.Line()
		p.err(line, offset, endoffset-offset, "syntax error")
		return false, 0
	}

	return true, p.stack[len(p.stack)-2].value
}

const errSymbol = 16

func (p *Parser) recover() bool {
	if p.next.symbol == noToken {
		p.next.symbol = int32(p.lexer.Next())
		p.next.offset, p.next.endoffset = p.lexer.Pos()
	}
	if p.next.symbol == eoiToken {
		return false
	}
	e, _ := p.lexer.Pos()
	s := e
	for len(p.stack) > 0 && p.gotoState(p.stack[len(p.stack)-1].state, errSymbol) == -1 {
		// TODO cleanup
		p.stack = p.stack[:len(p.stack)-1]
		if len(p.stack) > 0 {
			s = p.stack[len(p.stack)-1].sym.offset
		}
	}
	if len(p.stack) > 0 {
		state := p.gotoState(p.stack[len(p.stack)-1].state, errSymbol)
		p.stack = append(p.stack, node{
			sym:   symbol{errSymbol, s, e},
			state: state,
		})
		return true
	}
	return false
}

func (p *Parser) action(state int32) int32 {
	a := tmAction[state]
	if a < -2 {
		// Lookahead is needed.
		if p.next.symbol == noToken {
			p.next.symbol = int32(p.lexer.Next())
			p.next.offset, p.next.endoffset = p.lexer.Pos()
		}
		a = -a - 3
		for ; tmLalr[a] >= 0; a += 2 {
			if tmLalr[a] == p.next.symbol {
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
	p.listener.Node(nt, node.sym.offset, node.sym.endoffset)
}
