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

var goImportTests = []struct {
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

func TestExtractGoImports(t *testing.T) {
	for _, tc := range goImportTests {
		if got := gen.ExtractGoImports(tc.input); got != tc.want {
			t.Errorf("ExtractImports(%q) = %q, want: %q", tc.input, got, tc.want)
		}
	}
}

func BenchmarkExtractGoImports(b *testing.B) {
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

var tsImportTests = []struct {
	name  string
	input string
	want  string
}{
	{
		name: "single import",
		input: `
const foo = "./foo".Bar;`,
		want: `
import {Bar} from "./foo";

const foo = Bar;`,
	},
	{
		name: "skipping comments",
		input: `// abc
// def
const foo = "./foo".Bar;`,
		want: `// abc
// def
import {Bar} from "./foo";

const foo = Bar;`,
	},
	{
		name: "pre-existing imports",
		input: `// abc
// def
import {Baz} from "./foo2";
const foo = "./foo".Bar;`,
		want: `// abc
// def
import {Bar} from "./foo";
import {Baz} from "./foo2";
const foo = Bar;`,
	},
	{
		name: "multiple imports from same module",
		input: `const x = "./foo".Bar;
const y = "./foo".Baz;`,
		want: `import {Bar, Baz} from "./foo";

const x = Bar;
const y = Baz;`,
	},
	{
		name: "imports from different modules",
		input: `const a = "./foo".Bar;
const b = "./bar".Qux;`,
		want: `import {Qux} from "./bar";
import {Bar} from "./foo";

const a = Bar;
const b = Qux;`,
	},
	{
		name:  "nested module paths",
		input: `const a = "./deep/nested/path".Component;`,
		want: `import {Component} from "./deep/nested/path";

const a = Component;`,
	},
	{
		name:  "no imports",
		input: `const a = 5; function test() { return true; }`,
		want:  `const a = 5; function test() { return true; }`,
	},
	{
		name:  "function call with imported symbol",
		input: `function process() { return "./utils".formatData(data); }`,
		want: `import {formatData} from "./utils";

function process() { return formatData(data); }`,
	},
	{
		name: "import in a complex expression",
		input: `const handler = (event) => {
  "./events".EventBus.publish(new "./models".Event());
};`,
		want: `import {EventBus} from "./events";
import {Event} from "./models";

const handler = (event) => {
  EventBus.publish(new Event());
};`,
	},
}

func TestExtractTsImports(t *testing.T) {
	for _, tc := range tsImportTests {
		t.Run(tc.name, func(t *testing.T) {
			got := gen.ExtractTsImports(tc.input)
			if got != tc.want {
				t.Errorf("ExtractTsImports() =\n%s\nwant:\n%s", got, tc.want)
			}
		})
	}
}

func BenchmarkExtractTsImports(b *testing.B) {
	const input = `const Component = "./components".Component;
const { useState, useEffect } = "./react".React;
function App() {
  const [data, setData] = useState(null);
  const formatter = new "./utils".Formatter();

  useEffect(() => {
    "./api".fetchData().then(setData);
  }, []);

  return formatter.format(data);
}`

	for i := 0; i < b.N; i++ {
		gen.ExtractTsImports(input)
	}
	b.SetBytes(int64(len(input)))
}
