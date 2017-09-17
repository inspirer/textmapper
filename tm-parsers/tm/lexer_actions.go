package tm

import "unicode/utf8"

func (l *Lexer) skipAction() bool {
	open := 1
	var openQuote rune
	for open > 0 {
		var skipNext bool
		switch l.ch {
		case -1:
			return false
		case '{':
			if openQuote == 0 {
				open++
			}
		case '}':
			if openQuote == 0 {
				open--
			}
		case '\'', '"':
			if openQuote == 0 {
				openQuote = l.ch
			} else if l.ch == openQuote {
				openQuote = 0
			}
		case '\\':
			skipNext = openQuote != 0
		case '\n':
			l.line++
		}

		// Scan the next character.
		// Note: the following code is inlined to avoid performance implications.
	next:
		l.offset = l.scanOffset
		if l.offset < len(l.source) {
			r, w := rune(l.source[l.offset]), 1
			if r >= 0x80 {
				// not ASCII
				r, w = utf8.DecodeRuneInString(l.source[l.offset:])
			}
			l.scanOffset += w
			l.ch = r
			if skipNext {
				skipNext = false
				goto next
			}
		} else {
			l.ch = -1 // EOI
		}

	}
	return true
}
