package lex

import (
	"fmt"
	"strconv"
	"strings"
	"unicode"
	"unicode/utf8"
)

// Regexp is a parsed regular expression suitable for use in a generated lexer. The set of supported
// features is very close to POSIX + UnicodeGroups, but it also has some lexer-specific extensions.
//
// Note: we cannot use "regexp/syntax" since it does not correctly parse /{hex}{4}/ but we adhere
// to the same structure for simplicity.
type Regexp struct {
	op       op         // operator
	sub      []*Regexp  // subexpressions, if any
	sub0     [1]*Regexp // storage for short sub
	charset  charset    // for OpCharClass
	charset0 [2]rune    // storage for short character sets
	min, max int        // min, max for OpRepeat
	text     string     // matched text for OpLiteral, or a reference for opExternal
}

// op is a single regular expression operator.
type op uint8

const (
	opLiteral   op = iota // matches text as a literal
	opCharClass           // matches any rune from a given character set
	opRepeat              // matches sub[0] at least min times, at most max (-1 means no limit)
	opConcat              // matches concatenation of subs
	opAlternate           // matches alternation of subs
	opExternal            // uses an external matcher, which name is specified in text

	// Temporary operator for parsing.
	opParen
)

type parseError struct {
	msg    string
	offset int
}

func (e parseError) Error() string {
	return fmt.Sprintf("broken regexp: %v", e.msg)
}

func (re Regexp) empty() bool {
	switch re.op {
	case opConcat, opAlternate:
		return len(re.sub) == 0
	case opLiteral:
		return len(re.text) == 0
	case opRepeat:
		return re.max == 0
	}
	return false
}

// ParseRegexp parses a regular expression from a string.
func ParseRegexp(input string) (*Regexp, error) {
	var buf [16]rune
	var p parser
	p.source = input
	p.set = buf[:0]
	p.next()
	re := p.parse()
	if p.err.msg != "" {
		return nil, p.err
	}
	return re, nil
}

type parser struct {
	source     string
	offset     int
	scanOffset int
	ch         rune
	set        []rune
	err        parseError
}

func (p *parser) next() {
	p.offset = p.scanOffset
	if p.offset < len(p.source) {
		r, w := rune(p.source[p.offset]), 1
		if r >= 0x80 {
			// not ASCII
			r, w = utf8.DecodeRuneInString(p.source[p.offset:])
			if r == utf8.RuneError && w == 1 {
				p.error("invalid rune")
			}
		}
		p.scanOffset += w
		p.ch = r
	} else {
		p.ch = -1 // EOI
	}
}

func (p *parser) parse() *Regexp {
	var fold bool
	var alloc [8]*Regexp
	stack := append(alloc[:0], &Regexp{op: opParen})
	var literalStart int
	var foldStack []bool

	for p.ch != -1 {
		switch p.ch {
		case '.':
			re := &Regexp{op: opCharClass, charset: []rune{0, '\n' - 1, '\n' + 1, unicode.MaxRune}}
			stack = append(stack, re)
		case '(':
			p.next()
			var setFold, neg bool
			if p.ch == '?' {
				for p.next(); p.ch != ':' && p.ch != ')'; p.next() {
					switch p.ch {
					case 'i':
						setFold = true
					case '-':
						neg = true
					default:
						p.error("unknown perl flags")
						return nil
					}
				}
				if p.ch == ')' {
					fold = true
					break
				} else {
					p.next()
				}
			}
			foldStack = append(foldStack, fold)
			if setFold {
				fold = !neg
			}
			stack = append(stack, &Regexp{op: opParen})
			continue
		case '|':
			stack = reduce(stack)
		case ')':
			stack = reduce(stack)
			if len(stack) == 1 {
				p.error("unexpected closing parenthesis")
				return nil
			}
			fold = foldStack[len(foldStack)-1]
			foldStack = foldStack[:len(foldStack)-1]
			last := len(stack) - 1
			stack[last].op = opAlternate
			if len(stack[last].sub) == 1 {
				stack[last] = stack[last].sub[0]
			}
		case '\\', '[':
			if p.ch == '\\' && p.scanOffset < len(p.source) && p.source[p.scanOffset] == 'Q' {
				// \Q ... \E
				var lit string
				literalStart = p.scanOffset + 1
				if i := strings.Index(p.source[literalStart:], `\E`); i < 0 {
					lit = p.source[literalStart:]
					p.scanOffset = len(p.source)
				} else {
					lit = p.source[literalStart : literalStart+i]
					p.scanOffset = literalStart + i + 2
				}
				p.next()
				stack = append(stack, &Regexp{op: opLiteral, text: lit})
				for lit != "" {
					r, size := utf8.DecodeRuneInString(lit)
					if r == utf8.RuneError && size == 1 {
						p.error("invalid rune")
						return nil
					}
					lit = lit[size:]
				}
				continue
			}

			var cs charset
			if p.ch == '\\' {
				cs = p.parseEscape(fold)
			} else {
				cs = p.parseClass(fold)
			}
			re := &Regexp{op: opCharClass}
			re.charset = append(re.charset0[:0], cs...)
			stack = append(stack, re)
			continue

		case '{':
			p.next()
			if p.ch >= '0' && p.ch <= '9' {
				last := len(stack) - 1
				if stack[last].op == opParen {
					p.error("unexpected quantifier")
					return nil
				}
				min, max := p.parseQuantifier()
				rep := &Regexp{op: opRepeat, max: max, min: min}
				rep.sub = append(rep.sub0[:0], stack[last])
				stack[last] = rep
				continue
			} else {
				start := p.offset
				for isid(p.ch) {
					p.next()
				}
				if p.ch != '}' || start == p.offset {
					p.error("invalid external regexp reference")
				}
				stack = append(stack, &Regexp{op: opExternal, text: p.source[start:p.offset]})
			}

		case '*', '+', '?':
			last := len(stack) - 1
			if stack[last].op != opParen {
				rep := &Regexp{op: opRepeat, max: -1}
				rep.sub = append(rep.sub0[:0], stack[last])
				switch p.ch {
				case '+':
					rep.min = 1
				case '?':
					rep.max = 1
				}
				stack[last] = rep
				break
			}
			fallthrough
		default:
			last := stack[len(stack)-1]
			if fold && foldable(p.ch) {
				re := &Regexp{op: opCharClass}
				re.charset = append(re.charset0[:0], p.ch, p.ch)
				re.charset.fold()
				stack = append(stack, re)
			} else if last.op == opLiteral && last.text == p.source[literalStart:p.offset] && p.canAppend() {
				last.text = p.source[literalStart:p.scanOffset]
			} else {
				literalStart = p.offset
				stack = append(stack, &Regexp{op: opLiteral, text: p.source[literalStart:p.scanOffset]})
			}
		}
		p.next()
	}
	stack = reduce(stack)
	if len(stack) != 1 {
		p.error("missing closing parenthesis")
		return nil
	}
	switch len(stack[0].sub) {
	case 1:
		return stack[0].sub[0]
	}
	stack[0].op = opAlternate
	return stack[0]
}

func (p *parser) parseQuantifier() (min, max int) {
	start := p.offset
	for p.ch >= '0' && p.ch <= '9' {
		p.next()
	}
	min, err := strconv.Atoi(p.source[start:p.offset])
	if err != nil {
		p.error("cannot parse quantifier")
		return
	}

	max = min
	if p.ch == ',' {
		p.next()
		max = -1
		start := p.offset
		for p.ch >= '0' && p.ch <= '9' {
			p.next()
		}
		if start < p.offset {
			if max, err = strconv.Atoi(p.source[start:p.offset]); err != nil {
				p.error("cannot parse quantifier")
				return
			}
			if max < min {
				p.error("invalid quantifier")
				return
			}
		}
	}

	if p.ch != '}' {
		p.error("cannot parse quantifier")
		return
	}
	p.next()
	return
}

func reduce(r []*Regexp) []*Regexp {
	paren := len(r) - 1
	for r[paren].op != opParen {
		paren--
	}
	if r[paren].sub == nil {
		r[paren].sub = r[paren].sub0[:0]
	}
	child := &Regexp{op: opConcat}
	ns := child.sub0[:0]
	for _, re := range r[paren+1:] {
		if !re.empty() {
			ns = append(ns, re)
		}
	}
	child.sub = ns
	if len(ns) == 1 {
		child = child.sub[0]
	}
	r[paren].sub = append(r[paren].sub, child)
	return r[:paren+1]
}

func (p *parser) canAppend() bool {
	if p.scanOffset == len(p.source) {
		return true
	}
	switch p.source[p.scanOffset] {
	case '*', '+', '?':
		return false
	case '{':
		if p.scanOffset+1 == len(p.source) {
			return true
		}
		ch := p.source[p.scanOffset+1]
		return ch < '0' || ch > '9'
	}
	return true
}

func (p *parser) error(msg string) {
	if len(p.err.msg) == 0 {
		p.err = parseError{msg: msg, offset: p.offset}
	}
}

func (p *parser) rune(r rune, fold bool) charset {
	p.set = append(p.set[:0], r, r)
	cs := charset(p.set)
	if fold {
		cs.fold()
	}
	return cs
}

func (p *parser) parseClass(fold bool) charset {
	p.next() // skip [
	var negated bool
	if p.ch == '^' {
		negated = true
		p.next()
	}
	var alloc [16]rune
	r := alloc[:0]
	if p.ch == ']' {
		r = append(r, p.ch, p.ch)
		p.next()
	}
	for p.ch != ']' {
		var lo rune
		switch p.ch {
		case '.':
			r = append(r, 0, '\n'-1, '\n'+1, unicode.MaxRune)
			p.next()
			continue
		case -1:
			p.error("missing closing bracket")
			return nil
		case '\\':
			cs := p.parseEscape(false)
			if !cs.oneRune() {
				r = append(r, cs...)
				continue
			}
			lo = cs[0]
		default:
			lo = p.ch
			p.next()
		}

		if so := p.scanOffset; p.ch != '-' || so == len(p.source) || p.source[so] == ']' {
			r = appendRange(r, lo, lo)
			continue
		}

		p.next()
		var hi rune
		if p.ch == '\\' {
			cs := p.parseEscape(false)
			if !cs.oneRune() {
				p.error("invalid character class range")
				return nil
			}
			hi = cs[0]
		} else {
			hi = p.ch
		}

		if hi < lo {
			p.error("invalid character class range")
			return nil
		}
		r = appendRange(r, lo, hi)
	}

	cs := newCharset(r)
	if fold {
		cs.fold()
	}
	if negated {
		cs.invert()
	}
	p.next()
	return cs
}

func (p *parser) parseEscape(fold bool) charset {
	p.next() // skip \
	var r rune
	switch p.ch {
	case '0', '1', '2', '3', '4', '5', '6', '7':
		for i := 0; i < 3; i++ {
			d := octval(p.ch)
			if d == -1 {
				p.error("invalid escape sequence")
				return nil
			}
			r = r<<3 + d
			p.next()
		}
		return p.rune(r, fold)
	case 'p', 'P':
		negated := p.ch == 'P'
		p.next()
		var name string
		if p.ch == '{' {
			p.next()
			if p.ch == '^' {
				negated = !negated
				p.next()
			}
			start := p.offset
			for isid(p.ch) {
				p.next()
			}
			if p.ch != '}' || start == p.offset {
				p.error("invalid \\p{} range")
			}
			name = p.source[start:p.offset]
		} else {
			name = p.source[p.offset:p.scanOffset]
		}
		p.next()
		var err error
		p.set, err = appendNamedSet(p.set[:0], name, fold)
		if err != nil {
			p.error(err.Error())
		}
		ret := newCharset(p.set)
		if negated {
			ret.invert()
		}
		return ret

	case 'd':
		p.next()
		return charset(append(p.set[:0], '0', '9'))

	case 'D':
		p.next()
		return charset(append(p.set[:0], 0, '0'-1, '9'+1, unicode.MaxRune))

	case 'w':
		p.next()
		return charset(append(p.set[:0], '0', '9', 'A', 'Z', '_', '_', 'a', 'z'))

	case 'W':
		p.next()
		return charset(append(p.set[:0], 0, '0'-1, '9'+1, 'A'-1, 'Z'+1, '_'-1, '_'+1, 'a'-1, 'z'+1, unicode.MaxRune))

	case 's', 'S':
		negated := p.ch == 'S'
		p.next()
		ret := charset(append(p.set[:0], '\t', '\t', '\n', '\n', '\f', '\f', '\r', '\r', ' ', ' '))
		if negated {
			ret.invert()
		}
		return ret

	case 'x', 'u', 'U':
		var l = 2
		if p.ch == 'u' {
			l = 4
		} else if p.ch == 'U' {
			l = 8
		}
		p.next()
		if p.ch == '{' {
			p.next()
			for {
				d := hexval(p.ch)
				if d == -1 {
					p.error("invalid escape sequence")
					return nil
				}
				r = r<<4 + d
				p.next()
				if p.ch == '}' {
					break
				}
			}
			p.next()
		} else {
			for i := 0; i < l; i++ {
				d := hexval(p.ch)
				if d == -1 {
					p.error("invalid escape sequence")
					return nil
				}
				r = r<<4 + d
				p.next()
			}
		}
		return p.rune(r, fold)
	case 'a':
		r = '\a'
	case 'f':
		r = '\f'
	case 'n':
		r = '\n'
	case 'r':
		r = '\r'
	case 't':
		r = '\t'
	case 'v':
		r = '\v'
	case -1:
		p.error("trailing backslash at end of regular expression")
		return nil
	default:
		if p.ch >= utf8.RuneSelf || p.ch != '_' && isid(p.ch) {
			p.error("invalid escape sequence")
			return nil
		}
		r = p.ch
	}
	p.next()
	return p.rune(r, fold)
}

func hexval(r rune) rune {
	switch {
	case r >= 'a' && r <= 'f':
		return r - 'a' + 10
	case r >= 'A' && r <= 'Z':
		return r - 'A' + 10
	case r >= '0' && r <= '9':
		return r - '0'
	}
	return -1
}

func octval(r rune) rune {
	switch {
	case r >= '0' && r <= '7':
		return r - '0'
	}
	return -1
}
