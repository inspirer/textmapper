package js

import (
	"context"
	"fmt"

	"github.com/inspirer/textmapper/parsers/js/token"
)

// Parser is a table-driven LALR parser for Javascript.
type Parser struct {
	eh       ErrorHandler
	listener Listener
	next     symbol
}

func (p *Parser) Init(eh ErrorHandler, l Listener) {
	p.eh = eh
	p.listener = l
}

type session struct {
	shiftCounter int32
	cache        map[uint64]bool
}

func (p *Parser) parse(ctx context.Context, start, end int16, stream *TokenStream) error {
	var s session
	s.cache = make(map[uint64]bool)

	state := start
	var lastErr SyntaxError
	recovering := 0

	var alloc [startStackSize]stackEntry
	stack := append(alloc[:0], stackEntry{state: state})
	p.next = stream.next(stack, end)

	for state != end {
		action := tmAction[state]
		if action > tmActionBase {
			// Lookahead is needed.
			if p.next.symbol == noToken {
				p.next = stream.next(stack, end)
			}
			pos := action + p.next.symbol
			if pos >= 0 && pos < tmTableLen && int32(tmCheck[pos]) == p.next.symbol {
				action = int32(tmTable[pos])
			} else {
				action = tmDefAct[state]
			}
		} else {
			action = tmDefAct[state]
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
					p.next = stream.next(stack, end)
				}
				entry.sym.offset, entry.sym.endoffset = p.next.offset, p.next.offset
			} else {
				entry.sym.offset = rhs[0].sym.offset
				entry.sym.endoffset = rhs[ln-1].sym.endoffset
			}
			p.applyRule(ctx, rule, &entry, rhs, stream, &s)
			if debugSyntax {
				fmt.Printf("reduced to: %v\n", symbolName(entry.sym.symbol))
			}
			state = gotoState(stack[len(stack)-1].state, entry.sym.symbol)
			entry.state = state
			stack = append(stack, entry)

		} else if action < -1 {
			if s.shiftCounter++; s.shiftCounter&0x1ff == 0 {
				// Note: checking for context cancellation is expensive so we do it from time to time.
				select {
				case <-ctx.Done():
					return ctx.Err()
				default:
				}
			}

			// Shift.
			state = int16(-2 - action)
			stack = append(stack, stackEntry{
				sym:   p.next,
				state: state,
			})
			if debugSyntax {
				fmt.Printf("shift: %v (%s)\n", symbolName(p.next.symbol), stream.text(p.next))
			}
			stream.flush(ctx, p.next)
			if p.next.symbol != eoiToken {
				switch token.Type(p.next.symbol) {
				case token.NOSUBSTITUTIONTEMPLATE:
					p.listener(NoSubstitutionTemplate, p.next.offset, p.next.endoffset)
				case token.TEMPLATEHEAD:
					p.listener(TemplateHead, p.next.offset, p.next.endoffset)
				case token.TEMPLATEMIDDLE:
					p.listener(TemplateMiddle, p.next.offset, p.next.endoffset)
				case token.TEMPLATETAIL:
					p.listener(TemplateTail, p.next.offset, p.next.endoffset)
				}
				p.next.symbol = noToken
			}
			if recovering > 0 {
				recovering--
			}
		}

		if action == -1 || state == -1 {
			stream.recoveryMode = true
			if recovering == 0 {
				if p.next.symbol == noToken {
					p.next = stream.next(stack, end)
				}
				lastErr = SyntaxError{
					Line:      stream.line(),
					Offset:    p.next.offset,
					Endoffset: p.next.endoffset,
				}
				if !p.eh(lastErr) {
					stream.flush(ctx, p.next)
					return lastErr
				}
			}
			stack = p.recoverFromError(ctx, stream, stack, end)
			if stack == nil {
				stream.flush(ctx, p.next)
				return lastErr
			}
			stream.recoveryMode = false
			state = stack[len(stack)-1].state
			recovering = 4
		}
	}

	return nil
}

func (p *Parser) recoverFromError(ctx context.Context, stream *TokenStream, stack []stackEntry, endState int16) []stackEntry {
	var recoverSyms [1 + token.NumTokens/8]uint8
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
		p.next = stream.next(stack, endState)
	}
	// By default, insert 'error' in front of the next token.
	s := p.next.offset
	e := s
	for _, tok := range stream.pending {
		// Try to cover all nearby invalid tokens.
		if token.Type(tok.symbol) == token.INVALID_TOKEN {
			if s > tok.offset {
				s = tok.offset
			}
			e = tok.endoffset
		}
	}
	for {
		if endoffset := p.skipBrokenCode(ctx, stream, canRecover); endoffset > e {
			e = endoffset
		}

		var matchingPos int
		if debugSyntax {
			fmt.Printf("trying to recover on %v\n", symbolName(p.next.symbol))
		}
		for _, pos := range recoverPos {
			errState := gotoState(stack[pos-1].state, errSymbol)
			if _, ok := reduceAll(stack[:pos], gotoState(stack[pos-1].state, errSymbol), p.next.symbol, endState); ok {
				matchingPos = pos
				break
			}
			// Semicolon insertion is not reliable on broken input, try to look behind the semicolon.
			if stream.delayed.symbol != noToken {
				if _, ok := reduceAll(stack[:pos], errState, stream.delayed.symbol, endState); ok {
					// Note: semicolons get inserted right after the previous
					// token, so we don't need to flush pending tokens.
					p.next = stream.next(stack, endState)
					matchingPos = pos
					break
				}
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
		}
		if s != e {
			// Try to cover all trailing invalid tokens.
			for _, tok := range stream.pending {
				if token.Type(tok.symbol) == token.INVALID_TOKEN && tok.endoffset > e {
					e = tok.endoffset
				}
			}
		}
		if debugSyntax {
			for i := len(stack) - 1; i >= matchingPos; i-- {
				fmt.Printf("dropped from stack: %v\n", symbolName(stack[i].sym.symbol))
			}
			fmt.Println("recovered")
		}
		stream.flush(ctx, symbol{errSymbol, s, e})
		stack = append(stack[:matchingPos], stackEntry{
			sym:   symbol{errSymbol, s, e},
			state: gotoState(stack[matchingPos-1].state, errSymbol),
		})
		return stack
	}
}
