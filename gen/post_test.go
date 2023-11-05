package gen_test

import (
	"testing"

	"github.com/inspirer/textmapper/gen"
)

func TestFormat(t *testing.T) {
	const file = "a.go"
	const input = "package   foo\n  import \"fmt\"\n   // Aa returns a string.\nfunc Aa( ) string {   return fmt.Sprintf(\"%v\", 123) }"

	got := gen.FormatGo(file, input)
	const want = "package foo\n\nimport \"fmt\"\n\n// Aa returns a string.\nfunc Aa() string { return fmt.Sprintf(\"%v\", 123) }\n"

	if want != got {
		t.Errorf("Format() = %v, want: %v", got, want)
	}

	const errInput = "package   foo\n     bar("
	got = gen.FormatGo(file, errInput)
	const wantErr = "// go fmt failed with: a.go:2:6: expected declaration, found bar\n" + errInput
	if got != wantErr {
		t.Errorf("Format(%q) = %v, want: %v", errInput, got, wantErr)
	}
}

var importTests = []struct {
	input string
	want  string
}{
	{"package a\nvar f \"fmt\".Stringer", "package a\n\nimport (\n\t\"fmt\"\n)\nvar f fmt.Stringer"},
	{"package a\nvar f \"fmt/bar as foo\".Stringer", "package a\n\nimport (\n\tfoo \"fmt/bar\"\n)\nvar f foo.Stringer"},
	// No package.
	{"var f \"abc/def\".Foo", "import (\n\t\"abc/def\"\n)\n\nvar f def.Foo"},
	// Several imports.
	{`// foo is great!
package foo

var a = make(map[string]"github.com/de-f/abc".Imp
func f(q []"abc/def as foo".Temp) {}`, `// foo is great!
package foo

import (
	foo "abc/def"

	"github.com/de-f/abc"
)

var a = make(map[string]abc.Imp
func f(q []foo.Temp) {}`},
}

func TestExtractImports(t *testing.T) {
	for _, tc := range importTests {
		if got := gen.ExtractGoImports(tc.input); got != tc.want {
			t.Errorf("ExtractImports(%q) = %q, want: %q", tc.input, got, tc.want)
		}
	}
}

func BenchmarkExtractImports(b *testing.B) {
	const input = `package a

func TestExtractImports(t *"testing"".T) {
	for _, tc := range importTests {
		if got := "foo/bar/gen as foo".ExtractImports(tc.input); got != tc.want {
			t.Errorf("ExtractImports(%q) = %q, want: %q", tc.input, got, tc.want)
		}
	}
}`
	for i := 0; i < b.N; i++ {
		gen.ExtractGoImports(input)
	}
	b.SetBytes(int64(len(input))) // ~44 MB/s
}
