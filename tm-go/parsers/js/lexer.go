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
    hash := uint32(0)
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
    	hash = hash*uint32(31) + uint32(l.ch)

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
		hh := hash&63
		switch hh {
		case 1:
			if hash == 0x5c13d641 && bytes.Equal([]byte("default"), l.source[l.tokenOffset:l.offset]) {
				rule = 13
				break
			}
			if hash == 0x2f9501 && bytes.Equal([]byte("enum"), l.source[l.tokenOffset:l.offset]) {
				rule = 40
				break
			}
		case 3:
			if hash == 0xcd244983 && bytes.Equal([]byte("finally"), l.source[l.tokenOffset:l.offset]) {
				rule = 19
				break
			}
		case 6:
			if hash == 0x37b0c6 && bytes.Equal([]byte("with"), l.source[l.tokenOffset:l.offset]) {
				rule = 37
				break
			}
		case 7:
			if hash == 0x33c587 && bytes.Equal([]byte("null"), l.source[l.tokenOffset:l.offset]) {
				rule = 41
				break
			}
		case 9:
			if hash == 0x18cc9 && bytes.Equal([]byte("for"), l.source[l.tokenOffset:l.offset]) {
				rule = 20
				break
			}
		case 11:
			if hash == 0xc8b && bytes.Equal([]byte("do"), l.source[l.tokenOffset:l.offset]) {
				rule = 15
				break
			}
		case 13:
			if hash == 0x6da5f8d && bytes.Equal([]byte("yield"), l.source[l.tokenOffset:l.offset]) {
				rule = 38
				break
			}
		case 14:
			if hash == 0x36758e && bytes.Equal([]byte("true"), l.source[l.tokenOffset:l.offset]) {
				rule = 42
				break
			}
		case 17:
			if hash == 0xcb7e7191 && bytes.Equal([]byte("target"), l.source[l.tokenOffset:l.offset]) {
				rule = 51
				break
			}
			if hash == 0xcccfb691 && bytes.Equal([]byte("typeof"), l.source[l.tokenOffset:l.offset]) {
				rule = 33
				break
			}
		case 20:
			if hash == 0x375194 && bytes.Equal([]byte("void"), l.source[l.tokenOffset:l.offset]) {
				rule = 35
				break
			}
		case 22:
			if hash == 0x58e7956 && bytes.Equal([]byte("await"), l.source[l.tokenOffset:l.offset]) {
				rule = 39
				break
			}
			if hash == 0x18f56 && bytes.Equal([]byte("get"), l.source[l.tokenOffset:l.offset]) {
				rule = 46
				break
			}
		case 23:
			if hash == 0xdd7 && bytes.Equal([]byte("of"), l.source[l.tokenOffset:l.offset]) {
				rule = 48
				break
			}
		case 24:
			if hash == 0x524f73d8 && bytes.Equal([]byte("function"), l.source[l.tokenOffset:l.offset]) {
				rule = 21
				break
			}
		case 25:
			if hash == 0xb22d2499 && bytes.Equal([]byte("extends"), l.source[l.tokenOffset:l.offset]) {
				rule = 18
				break
			}
		case 27:
			if hash == 0x1a21b && bytes.Equal([]byte("let"), l.source[l.tokenOffset:l.offset]) {
				rule = 47
				break
			}
		case 29:
			if hash == 0xd1d && bytes.Equal([]byte("if"), l.source[l.tokenOffset:l.offset]) {
				rule = 22
				break
			}
		case 30:
			if hash == 0x364e9e && bytes.Equal([]byte("this"), l.source[l.tokenOffset:l.offset]) {
				rule = 30
				break
			}
		case 32:
			if hash == 0x1a9a0 && bytes.Equal([]byte("new"), l.source[l.tokenOffset:l.offset]) {
				rule = 26
				break
			}
		case 33:
			if hash == 0x20a6f421 && bytes.Equal([]byte("debugger"), l.source[l.tokenOffset:l.offset]) {
				rule = 12
				break
			}
		case 34:
			if hash == 0x1bc62 && bytes.Equal([]byte("set"), l.source[l.tokenOffset:l.offset]) {
				rule = 49
				break
			}
		case 35:
			if hash == 0x5a73763 && bytes.Equal([]byte("const"), l.source[l.tokenOffset:l.offset]) {
				rule = 10
				break
			}
			if hash == 0x5cb1923 && bytes.Equal([]byte("false"), l.source[l.tokenOffset:l.offset]) {
				rule = 43
				break
			}
		case 37:
			if hash == 0xb96173a5 && bytes.Equal([]byte("import"), l.source[l.tokenOffset:l.offset]) {
				rule = 23
				break
			}
			if hash == 0xd25 && bytes.Equal([]byte("in"), l.source[l.tokenOffset:l.offset]) {
				rule = 24
				break
			}
		case 38:
			if hash == 0x693a6e6 && bytes.Equal([]byte("throw"), l.source[l.tokenOffset:l.offset]) {
				rule = 31
				break
			}
		case 39:
			if hash == 0xde312ca7 && bytes.Equal([]byte("continue"), l.source[l.tokenOffset:l.offset]) {
				rule = 11
				break
			}
			if hash == 0x1c727 && bytes.Equal([]byte("var"), l.source[l.tokenOffset:l.offset]) {
				rule = 34
				break
			}
		case 42:
			if hash == 0x3017aa && bytes.Equal([]byte("from"), l.source[l.tokenOffset:l.offset]) {
				rule = 45
				break
			}
		case 43:
			if hash == 0xb06685ab && bytes.Equal([]byte("delete"), l.source[l.tokenOffset:l.offset]) {
				rule = 14
				break
			}
		case 44:
			if hash == 0x35c3d12c && bytes.Equal([]byte("instanceof"), l.source[l.tokenOffset:l.offset]) {
				rule = 25
				break
			}
		case 46:
			if hash == 0xcacdce6e && bytes.Equal([]byte("static"), l.source[l.tokenOffset:l.offset]) {
				rule = 50
				break
			}
		case 48:
			if hash == 0x2e7b30 && bytes.Equal([]byte("case"), l.source[l.tokenOffset:l.offset]) {
				rule = 7
				break
			}
			if hash == 0xc84e3d30 && bytes.Equal([]byte("return"), l.source[l.tokenOffset:l.offset]) {
				rule = 27
				break
			}
		case 49:
			if hash == 0x6bdcb31 && bytes.Equal([]byte("while"), l.source[l.tokenOffset:l.offset]) {
				rule = 36
				break
			}
		case 50:
			if hash == 0xc32 && bytes.Equal([]byte("as"), l.source[l.tokenOffset:l.offset]) {
				rule = 44
				break
			}
		case 52:
			if hash == 0xb32913b4 && bytes.Equal([]byte("export"), l.source[l.tokenOffset:l.offset]) {
				rule = 17
				break
			}
			if hash == 0xcafbb734 && bytes.Equal([]byte("switch"), l.source[l.tokenOffset:l.offset]) {
				rule = 29
				break
			}
		case 56:
			if hash == 0x5a5a978 && bytes.Equal([]byte("class"), l.source[l.tokenOffset:l.offset]) {
				rule = 9
				break
			}
		case 57:
			if hash == 0x2f8d39 && bytes.Equal([]byte("else"), l.source[l.tokenOffset:l.offset]) {
				rule = 16
				break
			}
		case 59:
			if hash == 0x5a0eebb && bytes.Equal([]byte("catch"), l.source[l.tokenOffset:l.offset]) {
				rule = 8
				break
			}
			if hash == 0x68b6f7b && bytes.Equal([]byte("super"), l.source[l.tokenOffset:l.offset]) {
				rule = 28
				break
			}
			if hash == 0x1c1bb && bytes.Equal([]byte("try"), l.source[l.tokenOffset:l.offset]) {
				rule = 32
				break
			}
		case 63:
			if hash == 0x59a58ff && bytes.Equal([]byte("break"), l.source[l.tokenOffset:l.offset]) {
				rule = 6
				break
			}
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
