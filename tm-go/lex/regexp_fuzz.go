// +build gofuzz

// go-fuzz-build github.com/inspirer/textmapper/tm-go/lex
// go-fuzz -bin=./lex-fuzz.zip -workdir=.

package lex

func Fuzz(data []byte) int {
	_, err := ParseRegexp(string(data), true)
	if err != nil {
		return 0
	}
	return 1
}
