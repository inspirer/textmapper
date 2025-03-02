// generated by Textmapper; DO NOT EDIT

package simple

import (
	"strings"
	"unicode/utf8"

	"github.com/inspirer/textmapper/parsers/simple/token"
)

// Lexer uses a generated DFA to scan through a utf-8 encoded input string. If
// the string starts with a BOM character, it gets skipped.
type Lexer struct {
	source string

	ch          rune // current character, -1 means EOI
	offset      int  // character offset
	scanOffset  int  // scanning offset
	tokenOffset int  // last token byte offset
	line        int  // current line number (1-based)
	tokenLine   int  // last token line
	value       interface{}
}

var bomSeq = "\xef\xbb\xbf"

// Init prepares the lexer l to tokenize source by performing the full reset
// of the internal state.
func (l *Lexer) Init(source string) {
	l.source = source

	l.ch = 0
	l.offset = 0
	l.scanOffset = 0
	l.tokenOffset = 0
	l.line = 1
	l.tokenLine = 1

	if strings.HasPrefix(source, bomSeq) {
		l.offset += len(bomSeq)
	}

	l.rewind(l.offset)
}

// Next finds and returns the next token in l.source. The source end is
// indicated by Token.EOI.
//
// The token text can be retrieved later by calling the Text() method.
func (l *Lexer) Next() token.Type {
restart:
	l.tokenLine = l.line
	l.tokenOffset = l.offset

	state := 0
	for state >= 0 {
		var ch int
		if uint(l.ch) < tmRuneClassLen {
			ch = int(tmRuneClass[l.ch])
		} else if l.ch < 0 {
			state = int(tmLexerAction[state*tmNumClasses])
			continue
		} else {
			ch = mapRune(l.ch)
		}
		state = int(tmLexerAction[state*tmNumClasses+ch])
		if state > tmFirstRule {
			if l.ch == '\n' {
				l.line++
			}

			// Scan the next character.
			// Note: the following code is inlined to avoid performance implications.
			l.offset = l.scanOffset
			if l.offset < len(l.source) {
				r, w := rune(l.source[l.offset]), 1
				if r >= 0x80 {
					// not ASCII
					r, w = utf8.DecodeRuneInString(l.source[l.offset:])
				}
				l.scanOffset += w
				l.ch = r
			} else {
				l.ch = -1 // EOI
			}
		}
	}

	tok := token.Type(tmFirstRule - state)
	switch tok {
	case token.INVALID_TOKEN:
		if l.offset == l.tokenOffset {
			if l.ch == -1 {
				tok = token.EOI
			}
			l.rewind(l.scanOffset)
		}
	case 2:
		goto restart
	}
	return tok
}

// Pos returns the start and end positions of the last token returned by Next().
func (l *Lexer) Pos() (start, end int) {
	start = l.tokenOffset
	end = l.offset
	return
}

// Line returns the line number of the last token returned by Next() (1-based).
func (l *Lexer) Line() int {
	return l.tokenLine
}

// Text returns the substring of the input corresponding to the last token.
func (l *Lexer) Text() string {
	return l.source[l.tokenOffset:l.offset]
}

// Value returns the value associated with the last returned token.
func (l *Lexer) Value() interface{} {
	return l.value
}

// Copy forks the lexer in its current state.
func (l *Lexer) Copy() Lexer {
	ret := *l
	return ret
}

// rewind can be used in lexer actions to accept a portion of a scanned token, or to include
// more text into it.
func (l *Lexer) rewind(offset int) {
	if offset < l.offset {
		l.line -= strings.Count(l.source[offset:l.offset], "\n")
	} else {
		if offset > len(l.source) {
			offset = len(l.source)
		}
		l.line += strings.Count(l.source[l.offset:offset], "\n")
	}

	// Scan the next character.
	l.scanOffset = offset
	l.offset = offset
	if l.offset < len(l.source) {
		r, w := rune(l.source[l.offset]), 1
		if r >= 0x80 {
			// not ASCII
			r, w = utf8.DecodeRuneInString(l.source[l.offset:])
		}
		l.scanOffset += w
		l.ch = r
	} else {
		l.ch = -1 // EOI
	}
}
