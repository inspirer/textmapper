package ident

import (
	"fmt"
	"strings"
	"unicode"
	"unicode/utf8"
)

// Style enumerates all supported identifier styles.
type Style int

const (
	CamelCase        Style = iota // FooBar
	CamelLower                    // fooBar
	UpperCase                     // FOOBAR
	UpperUnderscores              // FOO_BAR
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

// Produce produces a valid identifier for a symbol name.
func Produce(name string, style Style) string {
	var inQuotes bool
	if ln := len(name); ln > 2 && (name[0] == '\'' && name[ln-1] == '\'' || name[0] == '"' && name[ln-1] == '"') {
		name = name[1 : ln-1]
		inQuotes = true
		if name == `\\` && style == UpperCase {
			// Compatibility with legacy Textmapper.
			return "ESC"
		}
		if len(name) == 2 && name[0] == '\\' && charName[rune(name[1])] != "" {
			// Compatibility with legacy Textmapper. Remove the ESC prefix.
			name = name[1:]
		}
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
	if inQuotes && len(name) == 1 {
		r, _ := utf8.DecodeRuneInString(name)
		if _, ok := charName[r]; !ok {
			write("char")
			if style == UpperCase || style == UpperUnderscores && r == '_' {
				// Insert an underscore anyway, as with UpperUnderscores.
				buf.WriteByte('_')
			}
		}
	}
	var cont bool
	for i, r := range name {
		if r >= 'a' && r <= 'z' || r >= 'A' && r <= 'Z' || r >= '0' && r <= '9' {
			// We want to split FOOBar into foo and bar.
			if unicode.IsUpper(r) && (i > 0 && !unicode.IsUpper(rune(name[i-1])) || i+1 < len(name) && !unicode.IsUpper(rune(name[i+1]))) {
				cont = false
			}
			if !cont && buf.Len() > 0 && style == UpperUnderscores || buf.Len() == 0 && r >= '0' && r <= '9' {
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
			if r == '$' || r == '_' && style == UpperCase {
				// Preserving underscores in UpperCase.
				buf.WriteByte('_')
			}
			continue
		}
		if r == '_' {
			buf.WriteRune('_')
			cont = true
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

// IsValid returns true if a given string is a valid identifier across all supported
// target languages.
func IsValid(id string) bool {
	for i, c := range id {
		if !unicode.IsLetter(c) && c != '_' && (i == 0 || !unicode.IsDigit(c)) {
			return false
		}
	}
	return id != ""
}
