package js

import (
	"bytes"
	"unicode/utf8"
)

const (
	State_initial = 0
	State_div = 1
	State_template = 2
	State_template_div = 3
)

// ErrorHandler is called every time a lexer or parser is unable to process
// some part of the input.
type ErrorHandler func(line, offset, len int, msg string)

// Lexer uses a generated DFA to scan through a utf-8 encoded input string. If
// the string starts with a BOM character, it gets skipped.
type Lexer struct {
	source []byte
	err    ErrorHandler

	ch          rune // current character, -1 means EOI
	offset      int  // character offset
	tokenOffset int  // last token offset
	line        int  // current line number (1-based)
	tokenLine   int  // last token line
	lineOffset  int  // current line offset
	scanOffset  int  // scanning offset
	value       interface{}

	State int // lexer state, modifiable
}

const bom = 0xfeff // byte order mark, permitted as a first character only
var bomSeq = []byte{0xEF, 0xBB, 0xBF}

// Init prepares the lexer l to tokenize source by performing the full reset
// of the internal state.
//
// Note that Init may call err one or more times if there are errors in the
// first few characters of the text.
func (l *Lexer) Init(source []byte, err ErrorHandler) {
	l.source = source
	l.err = err

	l.ch = 0
	l.offset = 0
	l.tokenOffset = 0
	l.line = 1
	l.tokenLine = 1
	l.lineOffset = 0
	l.scanOffset = 0
	l.State = 0

	if bytes.HasPrefix(source, bomSeq) {
		l.scanOffset += len(bomSeq)
	}

skipChar:
	l.offset = l.scanOffset
	if l.offset < len(l.source) {
		r, w := rune(l.source[l.offset]), 1
		if r >= 0x80 {
			// not ASCII
			r, w = utf8.DecodeRune(l.source[l.offset:])
			if r == utf8.RuneError && w == 1 || r == bom {
				l.invalidRune(r, w)
				l.scanOffset += w
				goto skipChar
			}
		}
		l.scanOffset += w
		l.ch = r
	} else {
		l.ch = -1 // EOI
	}
}

// Next finds and returns the next token in l.source. The source end is
// indicated by Token.EOI.
//
// The token text can be retrieved later by calling the Text() method.
func (l *Lexer) Next() Token {
	prevLine := l.tokenLine
restart:
	l.tokenLine = l.line
	l.tokenOffset = l.offset

	state := tmStateMap[l.State]
	for state >= 0 {
		var ch int
		switch {
		case l.ch < 0:
			ch = 0
		case int(l.ch) < len(tmRuneClass):
			ch = int(tmRuneClass[l.ch])
		default:
			ch = mapRune(l.ch)
		}
		state = int(tmLexerAction[state*tmNumClasses+ch])
		if state == -1 && ch == -1 {
			l.err(l.line, l.tokenOffset, l.offset-l.tokenOffset, "Unexpected end of input reached")
			return EOI
		}
		if state < -1 || ch == -1 {
			break
		}

		if l.ch == '\n' {
			l.line++
			l.lineOffset = l.offset
		}
	skipChar:
		// Scan the next character.
		// Note: the following code is inlined to avoid performance implications.
		l.offset = l.scanOffset
		if l.offset < len(l.source) {
			r, w := rune(l.source[l.offset]), 1
			if r >= 0x80 {
				// not ASCII
				r, w = utf8.DecodeRune(l.source[l.offset:])
				if r == utf8.RuneError && w == 1 || r == bom {
					l.invalidRune(r, w)
					l.scanOffset += w
					goto skipChar
				}
			}
			l.scanOffset += w
			l.ch = r
		} else {
			l.ch = -1 // EOI
		}
	}
	if state == -1 {
		l.err(l.tokenLine, l.tokenOffset, l.offset-l.tokenOffset, "invalid token")
		goto restart
	}
	if state == -2 {
		return EOI
	}

	rule := -state - 3
	switch rule {
	case 0:
		if r, ok := instancesOfIDENTIFIER[l.Text()]; ok {
			rule = r
		}
	}

	token := tmToken[rule]
	space := false
	switch rule {
	case 2: // WhiteSpace: /[\t\v\f \xa0\ufeff\p{Zs}]/
		space = true
	case 3: // LineTerminatorSequence: /[\n\r\u2028\u2029]|\r\n/
		space = true
	case 4: // MultiLineComment: /\/\*{commentChars}\*\//
		space = true
	case 5: // SingleLineComment: /\/\/[^\n\r\u2028\u2029]*/
		space = true
	}
	if space {
		goto restart
	}

	// There is an ambiguity in the language that a slash can either represent
	// a division operator, or start a regular expression literal. This gets
	// disambiguated at the grammar level - division always follows an
	// expression, while regex literals are expressions themselves. Here we use
	// some knowledge about the grammar to decide whether the next token can be
	// a regular expression literal.
	//
	// See the following thread for more details:
	// http://stackoverflow.com/questions/5519596/when-parsing-javascript-what

	inTemplate := l.State >= State_template
	var reContext bool
	switch token {
	case NEW, DELETE, VOID, TYPEOF, INSTANCEOF, IN, DO, RETURN, CASE, THROW, ELSE:
		reContext = true
	case TEMPLATEHEAD, TEMPLATEMIDDLE:
		reContext = true
		inTemplate = true
	case TEMPLATETAIL:
		reContext = false
		inTemplate = false
	case RPAREN, RBRACK:
		// TODO support if (...) /aaaa/;
		reContext = false
	case PLUSPLUS, MINUSMINUS:
		if prevLine != l.tokenLine {
			// This is a pre-increment/decrement, so we expect a regular expression.
			reContext = true
		} else {
			// If we were in reContext before this token, this is a
			// pre-increment/decrement, otherwise, this is a post. We can just
			// propagate the previous value of reContext.
			reContext = l.State == State_template || l.State == State_initial
		}
	default:
		reContext = token >= punctuationStart && token < punctuationEnd
	}
	if inTemplate {
		if reContext {
			l.State = State_template
		} else {
			l.State = State_template_div
		}
	} else if reContext {
		l.State = State_initial
	} else {
		l.State = State_div
	}
	return token
}

func (l *Lexer) invalidRune(r rune, w int) {
	switch r {
	case utf8.RuneError:
		l.err(l.line, l.offset, w, "illegal UTF-8 encoding")
	case bom:
		l.err(l.line, l.offset, w, "illegal byte order mark")
	}
}

// Pos returns the start and end positions of the last token returned by Next().
func (l *Lexer) Pos() (start, end int) {
	start = l.tokenOffset
	end = l.offset
	return
}

// Line returns the line number of the last token returned by Next().
func (l *Lexer) Line() int {
	return l.tokenLine
}

// Text returns the substring of the input corresponding to the last token.
func (l *Lexer) Text() string {
	return string(l.source[l.tokenOffset:l.offset])
}

func (l *Lexer) Value() interface{} {
	return l.value
}

var instancesOfIDENTIFIER = map[string]int{
	"break": 6,
	"case": 7,
	"catch": 8,
	"class": 9,
	"const": 10,
	"continue": 11,
	"debugger": 12,
	"default": 13,
	"delete": 14,
	"do": 15,
	"else": 16,
	"export": 17,
	"extends": 18,
	"finally": 19,
	"for": 20,
	"function": 21,
	"if": 22,
	"import": 23,
	"in": 24,
	"instanceof": 25,
	"new": 26,
	"return": 27,
	"super": 28,
	"switch": 29,
	"this": 30,
	"throw": 31,
	"try": 32,
	"typeof": 33,
	"var": 34,
	"void": 35,
	"while": 36,
	"with": 37,
	"yield": 38,
	"await": 39,
	"enum": 40,
	"null": 41,
	"true": 42,
	"false": 43,
	"as": 44,
	"from": 45,
	"get": 46,
	"let": 47,
	"of": 48,
	"set": 49,
	"static": 50,
	"target": 51,
}
