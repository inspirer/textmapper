package tm

import (
	"bytes"
	"unicode/utf8"
	"strconv"
)

const (
	State_initial = 0
	State_afterAt = 1
	State_afterAtID = 2
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
			ch = 1
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
		if r, ok := instancesOfID[l.Text()]; ok {
			rule = r
		}
	}

	token := tmToken[rule]
	space := false
	switch rule {
	case 0: // ID: /[a-zA-Z_]([a-zA-Z_\-0-9]*[a-zA-Z_0-9])?|'([^\n\\']|\\.)*'/
		{ l.value = l.Text(); }
	case 1: // regexp: /\/{reFirst}{reChar}*\//
		{ text := l.Text(); l.value = text[1:len(text)-2] }
	case 2: // scon: /"([^\n\\"]|\\.)*"/
		{ text := l.Text(); l.value = text[1:len(text)-2] }
	case 3: // icon: /\-?[0-9]+/
		{ l.value, _ = strconv.ParseInt(l.Text(), 10, 64) }
	case 5: // _skip: /[\n\r\t ]+/
		space = true
	case 6: // _skip_comment: /#.*(\r?\n)?/
		space = true
	case 7: // _skip_multiline: /\/\*{commentChars}\*\//
		space = true
	}
	if space {
		goto restart
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

var instancesOfID = map[string]int{
	"true": 39,
	"false": 40,
	"new": 41,
	"separator": 42,
	"as": 43,
	"import": 44,
	"set": 45,
	"brackets": 46,
	"inline": 47,
	"prec": 48,
	"shift": 49,
	"returns": 50,
	"input": 51,
	"left": 52,
	"right": 53,
	"nonassoc": 54,
	"generate": 55,
	"assert": 56,
	"empty": 57,
	"nonempty": 58,
	"global": 59,
	"explicit": 60,
	"lookahead": 61,
	"param": 62,
	"flag": 63,
	"no-eoi": 64,
	"soft": 65,
	"class": 66,
	"interface": 67,
	"void": 68,
	"space": 69,
	"layout": 70,
	"language": 71,
	"lalr": 72,
	"lexer": 73,
	"parser": 74,
	"reduce": 75,
}
