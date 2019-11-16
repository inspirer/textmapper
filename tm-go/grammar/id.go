package grammar

import (
	"fmt"
	"strings"
	"unicode"
)

// nameStyle enumerates all supported identifier styles.
type nameStyle int

const (
	CamelCase        nameStyle = iota // FooBar
	CamelLower                        // fooBar
	UpperCase                         // FOOBAR
	UpperUnderscores                  // FOO_BAR
)

// charName contains the default names of non-letter ascii symbols.
var charName = map[rune]string{
	'\t': "tab",
	'\n': "lf",
	'\r': "cr",
	' ':  "space",
	'!':  "excl",
	'"':  "quote",
	'#':  "sharp",
	'$':  "dollar",
	'%':  "rem",
	'&':  "and",
	'\'': "apos",
	'(':  "lparen",
	')':  "rparen",
	'*':  "mult",
	'+':  "plus",
	',':  "comma",
	'-':  "minus",
	'.':  "dot",
	'/':  "div",
	':':  "colon",
	';':  "semicolon",
	'<':  "lt",
	'=':  "assign",
	'>':  "gt",
	'?':  "quest",
	'@':  "atsign",
	'[':  "lbrack",
	'\\': "esc",
	']':  "rbrack",
	'^':  "xor",
	'`':  "bquote",
	'{':  "lbrace",
	'|':  "or",
	'}':  "rbrace",
	'~':  "tilde",
}

// SymbolID produces a valid identifier for a symbol name.
func SymbolID(name string, style nameStyle) string {
	var inQuotes bool
	if ln := len(name); ln > 2 && name[0] == '\'' && name[ln-1] == '\'' {
		name = name[1 : ln-1]
		inQuotes = true
	}
	var buf strings.Builder
	write := func(word string) {
		switch style {
		case CamelLower, CamelCase:
			if buf.Len() > 0 || style == CamelCase {
				buf.WriteString(strings.ToUpper(word[:1]))
				buf.WriteString(word[1:])
			} else {
				buf.WriteString(word)
			}
		case UpperUnderscores:
			if buf.Len() > 0 {
				buf.WriteByte('_')
			}
			fallthrough
		case UpperCase:
			buf.WriteString(strings.ToUpper(word))
		}
	}
	if !isIdent(name) {
		write("char")
		if style == UpperCase {
			// Insert an underscore anyway, as with UpperUnderscores.
			buf.WriteByte('_')
		}
	}
	var cont bool
	for i, r := range name {
		if r >= 'a' && r <= 'z' || r >= 'A' && r <= 'Z' || r >= '0' && r <= '9' && cont {
			// We want to split FOOBar into foo and bar.
			if unicode.IsUpper(r) && (i > 0 && !unicode.IsUpper(rune(name[i-1])) || i+1 < len(name) && !unicode.IsUpper(rune(name[i+1]))) {
				cont = false
			}
			if !cont && buf.Len() > 0 && style == UpperUnderscores {
				buf.WriteByte('_')
			}
			camel := style == CamelCase || style == CamelLower
			if camel && (cont || style == CamelLower && buf.Len() == 0) {
				buf.WriteRune(unicode.ToLower(r))
			} else {
				buf.WriteRune(unicode.ToUpper(r))
			}
			cont = true
			continue
		}
		cont = false
		if !inQuotes {
			continue
		}
		word := charName[r]
		if word == "" {
			if r <= 0xff {
				word = fmt.Sprintf("x%02x", r)
			} else {
				word = fmt.Sprintf("u%06x", r)
			}
		}
		write(word)
	}
	return buf.String()
}

// isIdent reports whether the given name is a valid Go identifier.
func isIdent(name string) bool {
	// From https://golang.org/ref/spec#Identifiers
	//
	//    identifier = letter { letter | unicode_digit } .
	//    letter     = unicode_letter | "_" .
	for i, r := range name {
		isLetter := unicode.IsLetter(r) || r == '_'
		if i == 0 && !isLetter {
			return false
		}
		isDigit := unicode.IsDigit(r)
		if !isLetter && !isDigit {
			return false
		}
	}
	return true
}
