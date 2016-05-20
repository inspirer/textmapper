package js_test

import (
	"reflect"
	"testing"

	"github.com/inspirer/textmapper/tm-go/parsers/js"
	"strings"
)

const separator rune = '“'
const nestedLeft rune = '«'
const nestedRight rune = '»'

type jsTestCase struct {
	nt     js.NodeType
	inputs []string
}

type jsTestExpectation struct {
	offset, endoffset int
}

var jsParseTests = []jsTestCase{
	{js.IdentifierName, []string{
		`const a = {“cc“: 5}.“cc“;`,
		`import {“a“ as b} from './Test1';`,
	}},
	{js.IdentifierReference, []string{
		`/*no expectations*/ const a = 15;`,
		`/*no expectations*/ var b = 7;`,
		`var e = “aa“;`,
		`for (; “b“ < “a“; “b“++) { };`,
		`/*no expectations*/ var c = (function() {})();`,
	}},
	{js.BindingIdentifier, []string{
		`const “a“ = 15;`,
		`var “b“ = 7;`,
		`var “e“ = aa;`,
		`/*no expectations*/ for (; b < a; b++) { }`,
		`var “c“ = (function() {})();`,
		`import {a as “b“} from './Test1';`,
	}},
	{js.LabelIdentifier, []string{
		`A: for (;;) {continue “A“; }`,
		`break “B“;`,
	}},
	{js.PrimaryExpression, []string{
		`const c = “e“[“0“] + “5“ + “this“.e;`,
		`const c = “[«0»]“[“0“ + “2“] + “{a: «33»}“.a;`,
	}},
	{js.CoverParenthesizedExpressionAndArrowParameterList, []string{
		`c = “(1+2)“;`,
		`c = “(a)“ => a + 5;`,
	}},
	{js.Literal, []string{
		`c = “1“ | a[“2“] | “true“ | “false“ | “1e4“;`,
	}},
	{js.ArrayLiteral, []string{
		`c = “[4]“.concat(“[1, 2, 3]“);`,
	}},
	{js.SpreadElement, []string{
		`c = [4,,,, “...a“];`,
	}},
	{js.ObjectLiteral, []string{
		`(“{}“);`,
		`a = “{}“;`,
		`for (let {} of “{}“);`,
		`a = “{a: [], c: 1}“;`,
		`a = «{a: «{}»,}»;`,
	}},
	{js.PropertyDefinition, []string{
		`{} ({“a“,“b: 1 in {}“, “f() {}“, });`,
	}},
	{js.LiteralPropertyName, []string{
		`{} ({a,“b“: 1 in {}, “f“() {}, get “d“() { return 1;}});`,
		`{} ({* “d“() { yield 1;}});`,
	}},
	{js.ComputedPropertyName, []string{
		`{} ({* “["a"+i]“() { yield 1;}});`,
	}},
	{js.CoverInitializedName, []string{
	// TODO
	}},
	{js.Initializer, []string{
	// TODO
	}},
	{js.TemplateLiteral, []string{
	// TODO
	}},
	{js.TemplateSpans, []string{
	// TODO
	}},
	{js.MemberExpression, []string{
	// TODO
	}},
	{js.SuperProperty, []string{
	// TODO
	}},
	{js.MetaProperty, []string{
	// TODO
	}},
	{js.NewTarget, []string{
	// TODO
	}},
	{js.NewExpression, []string{
	// TODO
	}},
	{js.CallExpression, []string{
	// TODO
	}},
	{js.SuperCall, []string{
	// TODO
	}},
	{js.Arguments, []string{
	// TODO
	}},
	{js.LeftHandSideExpression, []string{
		`“a“ = “5“;`,
		`“a“ += “5“;`,
		`“a“++;`,
		`“c“--;`,
		`“a()“;`,
		`“let()“;`,
		`“let“++;`,
		`for (“a“ in “b“) continue;`,
		`for (let [a] in “b“);`,
		`for (“let.b“ in “b“);`,
		`for (“a“ of “b“);`,
		`“new new A().q()“;`,
		`class A extends “B“ {}`,
	}},
	//PostfixExpression
	//UnaryExpression
	//MultiplicativeExpression
	//MultiplicativeOperator
	//AdditiveExpression
	//ShiftExpression
	//RelationalExpression
	//EqualityExpression
	//BitwiseANDExpression
	//BitwiseXORExpression
	//BitwiseORExpression
	//LogicalANDExpression
	//LogicalORExpression
	//ConditionalExpression
	//AssignmentExpression
	//AssignmentOperator
	{js.Expression, []string{
	// TODO `if (“a in b“);`,
	}},
	//Statement
	//Declaration
	//HoistableDeclaration
	//BreakableStatement
	//BlockStatement
	//Block
	//StatementListItem
	{js.LexicalDeclaration, []string{
		`“let a;“`,
		`“let a = 5;“`,
		`“let [a] = [5];“`,
		`“const {b: [c]} = a;“`,
	}},
	{js.LetOrConst, []string{
		`“let“ a;`,
		`“const“ {b} = a;`,
		`for (“const“ [of] of b);`,
		`for (“let“ [let] in b);`,
	}},
	{js.LexicalBinding, []string{
		`let “a“;`,
		`let “a = 5“;`,
		`let “[a] = [5]“;`,
		`const “{b: [c]} = a“;`,
	}},
	//VariableStatement
	//VariableDeclaration
	//BindingPattern
	{js.ObjectBindingPattern, []string{
		`const “{b: [c]}“ = a;`,
		`const “{}“ = a;`,
		`for (const “{b,}“ in a);`,
	}},
	//ArrayBindingPattern
	//BindingElisionElement
	//BindingProperty
	//BindingElement
	//SingleNameBinding
	//BindingRestElement
	{js.EmptyStatement, []string{
		`“;“`,
		`for(;;)“;“`,
		`while(true)“;“`,
		`do“;“ while(true);`,
	}},
	{js.ExpressionStatement, []string{
	// TODO
	}},
	{js.IfStatement, []string{
		`“if (a in b); else continue;“`,
		`“if (a in b); else «if (true) a(); else b();»“`,
		`“if (a in b) {«if(a);»} else continue;“`,
	}},
	{js.IterationStatement, []string{
		`“do {} while(a < 5);“`,
		`“do; while(a < 5);“`,
		`“while(false in a);“`,
		`“for (a in b) continue;“`,
		`“for (a=0; a < 5; a++);“`,
		`“for (var a; a < 5; );“`,
		`“for (var {a,b} = c; a < 5;);“`,
		`“for (let [a] in b);“`,
		`“for (let.a in b);“`,
		`“for (let a in b);“`,
		`“for (let in b);“`,
		`“for (var a in b);“`,
		`“for (var [a] in b);“`,
		`“for (a of b);“`,
		`“for (var {name:[name]} of b);“`,
		`“for (const [[name]] of b);“`,
	}},
	{js.ForDeclaration, []string{
		`for (“let a“ in b);`,
		`for (“const {}“ in b);`,
		`for (“let [a]“ in b);`,
		`for (“const [,,,,...a]“ in b);`,
	}},
	{js.ForBinding, []string{
		`for (var “a“ in b);`,
		`for (var “[...a]“ in b);`,
	}},
	{js.ContinueStatement, []string{
		`for(;;){ “continue;“ }`,
		`A: do “continue A;“ while(false);`,
	}},
	{js.BreakStatement, []string{
		`for(;;){ “break;“ }`,
		`A: do “break A;“ while(false);`,
	}},
	//ReturnStatement
	//WithStatement
	//SwitchStatement
	//CaseBlock
	//CaseClause
	//DefaultClause
	{js.LabelledStatement, []string{
		`“yield: do break yield; while(false);“`,
		`“A: for(;;) { break A; }“`,
		`“A: function q() { return; }“`,
	}},
	{js.LabelledItem, []string{
		`yield: “do break yield; while(false);“`,
		`A: “for(;;) { break A; }“`,
		`A: “function q() { return; }“`,
	}},
	//
	//ThrowStatement
	//TryStatement
	//Catch
	//Finally
	//CatchParameter
	//DebuggerStatement
	//FunctionDeclaration
	//FunctionExpression
	//StrictFormalParameters
	//FormalParameterList
	//FunctionRestParameter
	//FormalParameter
	{js.ArrowFunction, []string{
		`(“a => a + 1“)(1);`,
		`(“(a,b) => { return a*b; }“)(1);`,
	}},
	{js.ArrowParameters, []string{
		`(“a“ => a + 1)(1);`,
		`(“(a,b)“ => { return a*b; })(1);`,
	}},
	{js.ConciseBody, []string{
		`(a => “a + 1“)(1);`,
		`((a,b) => “{ return a*b; }“)(1);`,
	}},
	//MethodDefinition
	//PropertySetParameterList
	//GeneratorMethod
	//GeneratorDeclaration
	//GeneratorExpression
	//GeneratorBody
	//YieldExpression
	{js.ClassDeclaration, []string{
		`“class A {}“`,
		`“class A extends B {; ;}“`,
		`“class A extends B { a() {} }“`,
		`export default “class extends B {}“`,
		`export default “class { get a() { return 1;}}“`,
	}},
	{js.ClassExpression, []string{
		`(“class A {}“);`,
		`(“class A extends B {; ;}“);`,
		`(“class A extends B { a() {} }“);`,
		`(“class extends B {}“);`,
		`(“class { get a() { return 1;}}“);`,
	}},
	{js.ClassTail, []string{
		`class A “{}“`,
		`class A “extends B {; ;}“`,
		`class A “extends B { a() {} }“`,
		`export default class “extends B {}“`,
		`export default class “{ get a() { return 1;}}“`,
	}},
	{js.ClassHeritage, []string{
		`class A “extends B“ {; ;}`,
		`class A “extends compose(B,C)“ {}`,
		`(class “extends (A)“ {});`,
	}},
	//ClassBody
	//ClassElement
	//Module
	//ModuleBody
	//ModuleItem
	//ImportDeclaration
	//ImportClause
	//ImportedDefaultBinding
	//NameSpaceImport
	//NamedImports
	//FromClause
	//ImportSpecifier
	//ModuleSpecifier
	//ImportedBinding
	//ExportDeclaration
	//ExportClause
	//ExportSpecifier
}

func splitInput(input string, t *testing.T) (out []byte, exp []jsTestExpectation) {
	var stack []int
	for index, ch := range input {
		switch ch {
		case separator, nestedRight, nestedLeft:
			if ch == nestedLeft || ch == separator && len(stack) == 0 {
				stack = append(stack, len(out))
			} else if len(stack) == 0 {
				t.Fatalf("Unexpected closing parenthesis at %d in `%s`", index, input)
			} else {
				exp = append(exp, jsTestExpectation{stack[len(stack)-1], len(out)})
				stack = stack[:len(stack)-1]
			}
			continue
		}
		out = append(out, string(ch)...)
	}
	if len(stack) > 0 {
		t.Fatalf("Missing closing separator at %d in `%s`", stack[len(stack)-1], input)
	}
	return
}

func TestSplitInput(t *testing.T) {
	res, exp := splitInput(`abc“def“cdf“q1“q2`, t)
	if string(res) != `abcdefcdfq1q2` {
		t.Errorf("Unexpected result: %s", res)
	}
	if !reflect.DeepEqual(exp, []jsTestExpectation{{3, 6}, {9, 11}}) {
		t.Errorf("Unexpected expectations: %v", exp)
	}

	res, exp = splitInput(``, t)
	if string(res) != `` || len(exp) != 0 {
		t.Errorf("splitInput(``) is broken: %v", res)
	}

	res, exp = splitInput(`“abc“ «a«b«c»»»`, t)
	if string(res) != `abc abc` {
		t.Errorf("Unexpected result: %s", res)
	}
	if !reflect.DeepEqual(exp, []jsTestExpectation{{0, 3}, {6, 7}, {5, 7}, {4, 7}}) {
		t.Errorf("Unexpected expectations: %v", exp)
	}
}

type expTest struct {
	source       []byte
	expectedType js.NodeType
	exp          []jsTestExpectation
	t            *testing.T
}

func (e *expTest) Node(nt js.NodeType, offset, endoffset int) {
	//fmt.Println(nt.String())
	if e.expectedType != nt {
		return
	}
	if len(e.exp) == 0 {
		e.t.Errorf("Unexpected %v: `%s`", nt, e.source[offset:endoffset])
	} else if e.exp[0].offset != offset || e.exp[0].endoffset != endoffset {
		first := e.exp[0]
		e.t.Errorf("got `%s`, want `%s`", e.source[offset:endoffset], e.source[first.offset:first.endoffset])
	} else {
		e.exp = e.exp[1:]
	}
}

func (e *expTest) done() {
	if len(e.exp) > 0 {
		first := e.exp[0]
		e.t.Errorf("`%s` was not reported in `%s`", e.source[first.offset:first.endoffset], e.source)
	}
}

func TestParser(t *testing.T) {
	l := new(js.Lexer)
	p := new(js.Parser)
	for _, tc := range jsParseTests {
		for _, input := range tc.inputs {
			source, exp := splitInput(input, t)
			if len(exp) == 0 && !strings.HasPrefix(input, "/*no expectations*/") {
				t.Errorf("No expectations in `%s`", input)
			}

			onError := func(line, offset, len int, msg string) {
				t.Errorf("%d, %d: %s", line, offset, msg)
			}
			expTest := &expTest{source, tc.nt, exp, t}

			l.Init([]byte(source), onError)
			p.Init(onError, expTest)
			res, _ := p.Parse(l)
			if !res {
				t.Errorf("Parse() returned false for `%s`", source)
			} else {
				expTest.done()
			}
		}
	}
}
