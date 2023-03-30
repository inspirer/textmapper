package tm_test

import (
	"os"
	"path/filepath"
	"strings"
	"testing"

	"github.com/inspirer/textmapper/parsers/parsertest"
	"github.com/inspirer/textmapper/parsers/tm"
)

var parseTests = []struct {
	nt     tm.NodeType
	inputs []string
}{

	{tm.Identifier, []string{
		`  language «a»(«b»); :: lexer «error»: `,
	}},
	{tm.Option, []string{
		header + ` «a = 5»  «list = [5]»  «feature = true» `,
	}},
	{tm.IntegerLiteral, []string{
		header + ` a = «5»  list = [«5»]  feature = true `,
	}},
	{tm.BooleanLiteral, []string{
		header + ` a = «true»`,
	}},
	{tm.Lexeme, []string{
		lexerPre + ` «error:»`,
		lexerPre + ` «<foo, bar> error:»`,
		lexerPre + ` <foo, bar> { «error:» }`,
		lexerPre + ` «error: /abc/ -1»  «def:»`,
		lexerPre + ` «error: /abc/ {}»`,
		lexerPre + ` <*> { «error: /abc/ {}» }`,
		lexerPre + ` «int {Type}: /[0-9]+/ { $$ = parseInt(); }»`,
	}},
	{tm.Command, []string{
		lexerPre + ` abc: /abc/ «{}»`,
		lexerPre + ` abc: /abc/ «{ printf("}") }»`,
	}},
	{tm.Comment, []string{
		parserPre + ` «# abc»
		  «# abc2»
		  a : abc ;    «# 8»
		  «# abc2»`,
	}},
	{tm.MultilineComment, []string{
		parserPre + `a void : «/* te ** / st */» ;`,
		parserPre + `«/* abc */» a:b;`,

		// While recovering.
		parserPre + " a : (§:: a «/*aaa*/» b ) ; ",
	}},
	{tm.InvalidToken, []string{
		parserPre + "a : «'»\n   ;",
	}},

	{tm.Rule, []string{
		parserPre + " a : /* empty */ «»| «abc» | «abc -> def» ; ",
	}},
	{tm.DirectiveExpect, []string{
		parserPre + " «%expect 0;» ",
	}},
	{tm.DirectiveExpectRR, []string{
		parserPre + " «%expect-rr 8;» ",
	}},
	{tm.SyntaxProblem, []string{
		parserPre + " a : (§«:: a /*aaa*/ b» ) ; ",
		parserPre + " a : §«+ a» ; ",
		header + ` a = 5  «b §a b c = 5» :: lexer a: /a/`,
	}},

	// TODO add tests
}

func TestParser(t *testing.T) {
	var l tm.Lexer
	var p tm.Parser

	seen := make(map[tm.NodeType]bool)
	seen[tm.File] = true
	for _, tc := range parseTests {
		seen[tc.nt] = true
		for _, input := range tc.inputs {
			test := parsertest.New(t, tc.nt.String(), input)
			l.Init(test.Source())
			errHandler := func(se tm.SyntaxError) bool {
				test.ConsumeError(t, se.Offset, se.Endoffset)
				return true
			}
			p.Init(errHandler, func(nt tm.NodeType, offset, endoffset int) {
				if nt == tc.nt {
					test.Consume(t, offset, endoffset)
				}
			})
			test.Done(t, p.Parse(&l))
		}
	}
	for n := tm.NodeType(1); n < tm.NodeTypeMax; n++ {
		if !seen[n] {
			// TODO t.Errorf("%v is not tested", n)
		}
	}
}

const header = "language l(a); "
const lexerPre = "language l(a); :: lexer\n"
const parserPre = "language l(a); :: lexer a = /abc/ :: parser "

const wantTextmapperFiles = 31

func TestExistingFiles(t *testing.T) {
	var sources []string
	err := filepath.Walk("../..", func(path string, info os.FileInfo, err error) error {
		if info.IsDir() && info.Name() == "build" {
			return filepath.SkipDir
		}
		if !info.IsDir() && strings.HasSuffix(info.Name(), ".tm") {
			sources = append(sources, path)
		}
		return err
	})
	if err != nil {
		t.Errorf("cannot collect all Textmapper files: %v", err)
	}
	if len(sources) != wantTextmapperFiles {
		t.Errorf("found %v Textmapper files, want: %v\n%v", len(sources), wantTextmapperFiles, strings.Join(sources, "\n"))
	}

	for _, path := range sources {
		content, err := os.ReadFile(path)
		if err != nil {
			t.Errorf("cannot read %v: %v", path, err)
			continue
		}

		l := new(tm.Lexer)
		l.Init(string(content))
		p := new(tm.Parser)
		p.Init(tm.StopOnFirstError, func(nt tm.NodeType, offset, endoffset int) { /* noop */ })
		err = p.Parse(l)
		if err != nil {
			t.Errorf("%v: parser failed with %v", path, err)
		}
	}
}
