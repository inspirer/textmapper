package lex

import (
	"bytes"
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
	charset  charset    // for opCharClass, never goes past 0xff in bytes mode
	charset0 [2]rune    // storage for short character sets
	min, max int        // min, max for opRepeat
	text     string     // matched text for opLiteral and opBytesLiteral, or a reference for opExternal
	offset   int        // for opLiteral, opCharClass, and opExternal
}

// op is a single regular expression operator.
type op uint8

const (
	opLiteral      op = iota // matches text as a literal
	opBytesLiteral           // matches text as utf-8 bytes
	opCharClass              // matches any rune from a given character set
	opRepeat                 // matches sub[0] at least min times, at most max (-1 means no limit)
	opConcat                 // matches concatenation of subs
	opAlternate              // matches alternation of subs
	opExternal               // uses an external matcher, which name is specified in text

	// Temporary operator for parsing.
	opParen
)

// ParseError represents a syntax error in a regular expression.
type ParseError struct {
	Msg       string
	Offset    int
	EndOffset int
}

func (e ParseError) Error() string {
	return fmt.Sprintf("broken regexp: %v", e.Msg)
}

func (re *Regexp) empty() bool {
	switch re.op {
	case opConcat, opAlternate:
		return len(re.sub) == 0
	case opLiteral, opBytesLiteral:
		return len(re.text) == 0
	case opRepeat:
		return re.max == 0
	}
	return false
}

// Constant checks if this regular expression matches exactly one target string, and returns that
// string on success.
func (re *Regexp) Constant() (string, bool) {
	switch re.op {
	case opCharClass:
		if re.charset.oneRune() {
			return string(re.charset[0]), true
		}
		return "", false
	case opLiteral, opBytesLiteral:
		return re.text, true
	case opConcat:
		var text []string
		for _, sub := range re.sub {
			val, ok := sub.Constant()
			if !ok {
				return "", false
			}
			text = append(text, val)
		}
		return strings.Join(text, ""), true
	}
	return "", false
}

// MustParse is a panic-on-error version of ParseRegexp.
func MustParse(input string, opts CharsetOptions) *Regexp {
	re, err := ParseRegexp(input, opts)
	if err != nil {
		panic(fmt.Sprintf("%q: %v", input, err))
	}
	return re
}

// ParseRegexp parses a regular expression from a string.
func ParseRegexp(input string, opts CharsetOptions) (*Regexp, error) {
	var buf [16]rune
	var p parser
	p.source = input
	p.set = buf[:0]
	p.next()
	re := p.parse(opts)
	if p.err.Msg != "" {
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
	err        ParseError
}

func (p *parser) next() {
	p.offset = p.scanOffset
	if p.offset < len(p.source) {
		r, w := rune(p.source[p.offset]), 1
		if r >= 0x80 {
			// not ASCII
			r, w = utf8.DecodeRuneInString(p.source[p.offset:])
			if r == utf8.RuneError && w == 1 {
				p.error("invalid rune", p.offset, p.offset+1)
			}
		}
		p.scanOffset += w
		p.ch = r
	} else {
		p.ch = -1 // EOI
	}
}

func (p *parser) parse(opts CharsetOptions) *Regexp {
	var alloc [8]*Regexp
	stack := append(alloc[:0], &Regexp{op: opParen})
	var start int
	var foldStack []bool

	for p.ch != -1 {
		switch p.ch {
		case '.':
			re := &Regexp{op: opCharClass, charset: []rune{0, '\n' - 1, '\n' + 1, opts.maxRune()}, offset: p.offset}
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
						p.error("unknown perl flags", p.offset, p.scanOffset)
						return nil
					}
				}
				if p.ch == ')' {
					if setFold {
						opts.Fold = !neg
					}
					break
				} else {
					p.next()
				}
			}
			foldStack = append(foldStack, opts.Fold)
			if setFold {
				opts.Fold = !neg
			}
			stack = append(stack, &Regexp{op: opParen})
			continue
		case '|':
			stack = reduce(stack)
		case ')':
			stack = reduce(stack)
			if len(stack) == 1 {
				p.error("unexpected closing parenthesis", p.offset, p.scanOffset)
				return nil
			}
			opts.Fold = foldStack[len(foldStack)-1]
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
				start = p.scanOffset + 1
				if i := strings.Index(p.source[start:], `\E`); i < 0 {
					lit = p.source[start:]
					p.scanOffset = len(p.source)
				} else {
					lit = p.source[start : start+i]
					p.scanOffset = start + i + 2
				}
				p.next()
				stack = append(stack, &Regexp{op: literalOp(opts.ScanBytes), text: lit, offset: start})
				for lit != "" {
					r, size := utf8.DecodeRuneInString(lit)
					if r == utf8.RuneError && size == 1 {
						p.error("invalid rune", start, start+1)
						return nil
					}
					lit = lit[size:]
					start += size
				}
				continue
			}

			var cs charset
			re := &Regexp{op: opCharClass, offset: p.offset}
			if p.ch == '\\' {
				cs = p.parseEscape(opts, true /*standalone*/)

				// Handle unicode characters in bytes mode.
				if opts.ScanBytes && cs.oneRune() && cs[0] > 0x7f {
					re.op = opBytesLiteral
					re.text = string(cs[0])
					stack = append(stack, re)
					continue
				}
			} else {
				cs = p.parseClass(opts)
			}
			re.charset = append(re.charset0[:0], cs...)
			stack = append(stack, re)
			continue

		case '{':
			offset := p.offset
			p.next()
			if p.ch >= '0' && p.ch <= '9' {
				last := len(stack) - 1
				if stack[last].op == opParen {
					p.error("unexpected quantifier", p.offset-1, p.offset)
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
					p.error("invalid external regexp reference", offset, p.scanOffset)
				}
				stack = append(stack, &Regexp{op: opExternal, text: p.source[start:p.offset], offset: offset})
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
			if opts.Fold && foldable(p.ch, opts) {
				re := &Regexp{op: opCharClass, offset: p.offset}
				re.charset = append(re.charset0[:0], p.ch, p.ch)
				re.charset.fold(opts.ScanBytes)
				stack = append(stack, re)
			} else if last.op == literalOp(opts.ScanBytes) && last.text == p.source[start:p.offset] && p.canAppend() {
				last.text = p.source[start:p.scanOffset]
			} else {
				start = p.offset
				stack = append(stack, &Regexp{op: literalOp(opts.ScanBytes), text: p.source[start:p.scanOffset], offset: start})
			}
		}
		p.next()
	}
	stack = reduce(stack)
	if len(stack) != 1 {
		p.error("missing closing parenthesis", p.offset, p.offset)
		return nil
	}
	switch len(stack[0].sub) {
	case 1:
		return stack[0].sub[0]
	}
	stack[0].op = opAlternate
	return stack[0]
}

func literalOp(scanBytes bool) op {
	if scanBytes {
		return opBytesLiteral
	}
	return opLiteral
}

func (p *parser) parseQuantifier() (min, max int) {
	start := p.offset
	for p.ch >= '0' && p.ch <= '9' {
		p.next()
	}
	min, err := strconv.Atoi(p.source[start:p.offset])
	if err != nil {
		p.error("cannot parse quantifier", start, p.offset)
		return
	}

	max = min
	if p.ch == ',' {
		p.next()
		max = -1
		toStart := p.offset
		for p.ch >= '0' && p.ch <= '9' {
			p.next()
		}
		if toStart < p.offset {
			if max, err = strconv.Atoi(p.source[toStart:p.offset]); err != nil {
				p.error("cannot parse quantifier", toStart, p.offset)
				return
			}
			if max < min {
				p.error("invalid quantifier", start-1, p.scanOffset)
				return
			}
		}
	}

	if p.ch != '}' {
		p.error("cannot parse quantifier", start, p.scanOffset)
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

func (p *parser) error(msg string, offset, endOffset int) {
	if len(p.err.Msg) == 0 {
		p.err = ParseError{Msg: msg, Offset: offset, EndOffset: endOffset}
	}
}

func (p *parser) rune(r rune, opts CharsetOptions) charset {
	p.set = append(p.set[:0], r, r)
	cs := charset(p.set)
	if opts.Fold {
		cs.fold(opts.ScanBytes)
	}
	return cs
}

// Note: in "bytes mode", the returned charset never contains runes above 0xff.
func (p *parser) parseClass(opts CharsetOptions) charset {
	start := p.offset
	p.next() // skip [
	var negated bool
	if p.ch == '^' {
		negated = true
		p.next()
	}
	var subs []charset
	var alloc [16]rune
	r := alloc[:0]
	if p.ch == ']' {
		r = append(r, p.ch, p.ch)
		p.next()
	}
	fold := opts.Fold
	opts.Fold = false // perform folding once at the end of the outer class
	for p.ch != ']' {
		var lo rune
		loStart := p.offset
		switch p.ch {
		case '.':
			r = append(r, 0, '\n'-1, '\n'+1, opts.maxRune())
			p.next()
			continue
		case '-':
			p.next()
			switch p.ch {
			case '[':
				subs = append(subs, p.parseClass(opts))
				continue
			case '\\':
				cs := p.parseEscape(opts, false /*standalone*/)
				if !cs.oneRune() {
					// Note: parseEscape uses p.set as a temporary buffer. Make a copy.
					subs = append(subs, append(charset(nil), cs...))
					continue
				}
				lo = cs[0]
				r = append(r, '-', '-')
			default:
				r = append(r, '-', '-')
				continue
			}
		case -1:
			p.error("missing closing bracket", start, p.offset)
			return nil
		case '\\':
			cs := p.parseEscape(opts, false /*standalone*/)
			if !cs.oneRune() {
				r = append(r, cs...)
				continue
			}
			lo = cs[0]
		default:
			if p.ch > opts.maxRune() {
				p.error(fmt.Sprintf("invalid character \\u%x (exceeds \\u%x)", p.ch, opts.maxRune()), p.offset, p.scanOffset)
				return nil
			}
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
			cs := p.parseEscape(opts, false /*standalone*/)
			if !cs.oneRune() {
				p.error("invalid character class range", loStart, p.offset)
				return nil
			}
			hi = cs[0]
		} else {
			if p.ch > opts.maxRune() {
				p.error(fmt.Sprintf("invalid character \\u%x (exceeds \\u%x)", p.ch, opts.maxRune()), p.offset, p.scanOffset)
				return nil
			}
			hi = p.ch
			p.next()
		}

		if hi < lo {
			p.error("invalid character class range", loStart, p.offset)
			return nil
		}
		r = appendRange(r, lo, hi)
	}

	cs := newCharset(r)
	for _, sub := range subs {
		cs.subtract(sub)
	}
	if fold {
		cs.fold(opts.ScanBytes)
	}
	if negated {
		cs.invert(opts)
	}
	p.next()
	return cs
}

// parseEscape returns either a character set or a single rune.
//
// In "bytes mode":
//   - only single runes can exceed 0xff (in standalone mode only, not in character classes).
//   - non-trivial character sets never contains runes above 0xff.
func (p *parser) parseEscape(opts CharsetOptions, standalone bool) charset {
	start := p.offset
	p.next() // skip \
	var r rune
	switch p.ch {
	case '0', '1', '2', '3', '4', '5', '6', '7':
		for i := 0; i < 3; i++ {
			d := octval(p.ch)
			if d == -1 {
				p.error("invalid escape sequence", start, p.scanOffset)
				return nil
			}
			r = r<<3 + d
			p.next()
		}
		if r > 0xff {
			p.error("invalid escape sequence (max = \\377)", start, p.offset)
			return nil
		}
		return p.rune(r, opts)
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
			nameStart := p.offset
			for isid(p.ch) {
				p.next()
			}
			if p.ch != '}' || nameStart == p.offset {
				p.error("invalid \\p{} range", start, p.scanOffset)
			}
			name = p.source[nameStart:p.offset]
		} else {
			name = p.source[p.offset:p.scanOffset]
		}
		p.next()
		var err error
		p.set, err = appendNamedSet(p.set[:0], name, opts)
		if err != nil {
			p.error(err.Error(), start, p.offset)
		}
		ret := newCharset(p.set)
		if negated {
			ret.invert(opts)
		}
		return ret

	case 'd':
		p.next()
		return append(p.set[:0], '0', '9')

	case 'D':
		p.next()
		return append(p.set[:0], 0, '0'-1, '9'+1, opts.maxRune())

	case 'w':
		p.next()
		return append(p.set[:0], '0', '9', 'A', 'Z', '_', '_', 'a', 'z')

	case 'W':
		p.next()
		return append(p.set[:0], 0, '0'-1, '9'+1, 'A'-1, 'Z'+1, '_'-1, '_'+1, 'a'-1, 'z'+1, opts.maxRune())

	case 's', 'S':
		negated := p.ch == 'S'
		p.next()
		ret := charset(append(p.set[:0], '\t', '\t', '\n', '\n', '\v', '\v', '\f', '\f', '\r', '\r', ' ', ' '))
		if negated {
			ret.invert(opts)
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
			start := p.offset
			for {
				d := hexval(p.ch)
				if d == -1 {
					p.error("invalid escape sequence", start, p.scanOffset)
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
					p.error("invalid escape sequence", start, p.scanOffset)
					return nil
				}
				r = r<<4 + d
				p.next()
			}
		}
		if r > opts.maxRune() && !standalone || r > unicode.MaxRune {
			if opts.ScanBytes {
				p.error("invalid escape sequence (exceeds \\uff)", start, p.offset)
			} else {
				p.error("invalid escape sequence (exceeds unicode.MaxRune)", start, p.offset)
			}
			return nil
		}
		return p.rune(r, opts)
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
		p.error("trailing backslash at end of regular expression", start, p.offset)
		return nil
	default:
		if p.ch >= utf8.RuneSelf || p.ch != '_' && isid(p.ch) {
			p.error("invalid escape sequence", start, p.scanOffset)
			return nil
		}
		r = p.ch
	}
	p.next()
	return p.rune(r, opts)
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

func (re *Regexp) String() string {
	var b bytes.Buffer
	regexpString(re, &b, false)
	return b.String()
}

func regexpString(re *Regexp, b *bytes.Buffer, paren bool) {
	if paren {
		b.WriteString("(")
		defer b.WriteString(")")
	}
	switch re.op {
	case opLiteral, opBytesLiteral:
		b.WriteString(re.text)
	case opCharClass:
		if re.charset.oneRune() {
			b.WriteString(re.charset.String())
			break
		}
		fmt.Fprintf(b, "[%s]", re.charset)
	case opRepeat:
		var addParens bool
		switch re.sub[0].op {
		case opLiteral, opBytesLiteral:
			addParens = len(re.text) > 1
		case opAlternate, opConcat:
			addParens = true
		}
		regexpString(re.sub[0], b, addParens)
		switch {
		case re.min == 0 && re.max == -1:
			b.WriteString("*")
		case re.min == 1 && re.max == -1:
			b.WriteString("+")
		case re.min == 0 && re.max == 1:
			b.WriteString("?")
		case re.max == re.min:
			fmt.Fprintf(b, "{%v}", re.min)
		case re.max == -1:
			fmt.Fprintf(b, "{%v,}", re.min)
		default:
			fmt.Fprintf(b, "{%v,%v}", re.min, re.max)
		}
	case opConcat:
		for _, s := range re.sub {
			regexpString(s, b, s.op == opAlternate)
		}
	case opAlternate:
		for i, s := range re.sub {
			if i > 0 {
				b.WriteString("|")
			}
			regexpString(s, b, s.op == opAlternate)
		}
	case opExternal:
		fmt.Fprintf(b, "{%s}", re.text)
	default:
		b.WriteString("unknown")
	}
}
