package json

import (
	"fmt"
)

// Parser is a table-driven LALR parser for json.
type Parser struct {
	err ErrorHandler

	stack []node
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

func (p *Parser) Parse(lexer *Lexer) interface{} {
	return p.parse(0, 27, lexer)
}

func (p *Parser) parse(start, end int32, lexer *Lexer) interface{} {
	if cap(p.stack) < startStackSize {
		p.stack = make([]node, 0, startStackSize)
	}
	var state int32
	p.stack = append(p.stack[:0], node{state: state})

	token := lexer.Next()
	for state != end {
		action := p.action(state, token)

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
			// TODO apply rule
			p.stack = p.stack[:len(p.stack)-ln]
			state = p.gotoState(p.stack[len(p.stack)-1].state, node.symbol)
			node.state = state
			p.stack = append(p.stack, node)

		} else if action == -1 {
			// Shift.
			state = p.gotoState(state, int32(token))
			s, e := lexer.Pos()
			p.stack = append(p.stack, node{
				symbol:    int32(token),
				state:     state,
				offset:    s,
				endoffset: e,
			})
			if debugSyntax {
				fmt.Printf("shift: %v (%s)\n", token, lexer.Text())
			}
			if state != -1 && token != EOI {
				token = lexer.Next()
			}
		}

		if action == -2 || state == -1 {
			break
		}
	}

	if state != end {
		offset, endoffset := lexer.Pos()
		line := lexer.Line()
		p.err(line, offset, endoffset - offset, "syntax error")
		return nil
	}

	return p.stack[len(p.stack)-1].value
}

func (p *Parser) action(state int32, next Token) int32 {
	a := tmAction[state]
	if a < -2 {
		// Lookahead is needed.
		a = -a - 3
		for ; tmLalr[a] >= 0; a += 2 {
			if tmLalr[a] == int32(next) {
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
