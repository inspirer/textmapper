package parser

import (
	"fmt"
	"github.com/inspirer/textmapper/tm-go/status"
	"strings"
	"testing"
)

var offsetTests = []struct {
	input string
	want  string
}{
	{"", "[0]"},
	{"abc", "[0]"},
	{"abc\n", "[0 4]"},
	{"\nabc", "[0 1]"},
	{"\n\nabc\n", "[0 1 2 6]"},
}

func TestFile(t *testing.T) {
	for _, test := range offsetTests {
		lo := lineOffsets(test.input)
		if got := fmt.Sprintf("%v", lo); got != test.want {
			t.Errorf("lineOffsets(%q) = %v, want: %v", test.input, got, test.want)
		}
	}
}

const testFile = "abc"

var rangeTests = []struct {
	content string
	substr  string
	want    status.SourceRange
}{
	{"a", "a", status.SourceRange{testFile, 0, 1, 1, 1}},
	{"abcdef", "def", status.SourceRange{testFile, 3, 6, 1, 4}},
	{"abc\ndef", "def", status.SourceRange{testFile, 4, 7, 2, 1}},
	{"\n\n\n def", "def", status.SourceRange{testFile, 4, 7, 4, 2}},
}

func TestSourceRange(t *testing.T) {
	for _, test := range rangeTests {
		f := newFile(testFile, test.content)
		i := strings.Index(test.content, test.substr)
		if i == -1 {
			t.Fatalf("%q is not found in %q", test.substr, test.content)
		}
		if got := f.sourceRange(i, i+len(test.substr)); got != test.want {
			t.Errorf("sourceRange(%q,%q) = %v, want: %v", test.content, test.substr, got, test.want)
		}
	}
}
