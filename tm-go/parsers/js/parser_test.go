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
		`var e = “let“;`,
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
	{js.ThisExpression, []string{
		`const c = “this“.e;`,
	}},
	{js.RegularExpression, []string{
	// TODO
	}},
	{js.ParenthesizedExpression, []string{
		`c = “(1+2)“;`,
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
		`{} ({“a“,“b: 1 in {}“, f() {}, });`,
	}},
	{js.SyntaxError, []string{
		`{a=b;} ({“a = b“});`,
	}},
	{js.LiteralPropertyName, []string{
		`{} ({a,“b“: 1 in {}, “f“() {}, get “d“() { return 1;}});`,
		`{} ({* “d“() { yield 1;}});`,
	}},
	{js.ComputedPropertyName, []string{
		`{} ({* “["a"+i]“() { yield 1;}});`,
	}},
	{js.CoverInitializedName, []string{
		`({“a = b“, “yield = {yield}“});`,
	}},
	{js.Initializer, []string{
		`({a “= b“, yield “= {yield}“});`,
		`var a “= 4“, b “=[]“;`,
	}},
	{js.TemplateLiteral, []string{
	// TODO
	}},
	{js.TemplateSpans, []string{
	// TODO
	}},
	{js.TaggedTemplate, []string{
	// TODO
	}},
	{js.IndexAccess, []string{
		`“super[10]“();`,
		`“super()[10]“();`,
		`“s.a()[10]“();`,
		`“«s.a()[10]»[10]“();`,
	}},
	{js.PropertyAccess, []string{
		`for (««let.a».b» in b);`,
		`“a.b“();`,
		`“a().b“();`,
		`“a()[10].b“();`,
		`“super.me“();`,
	}},
	{js.SuperExpression, []string{
		`“super“.me();`,
	}},
	{js.NewExpression, []string{
		`“new A“;`,
		`“new A.b[10]“;`,
		`“new A()“;`,
		`“new A.b[10]()“.a();`,
		`“new A.b[10]()“[10];`,
	}},
	{js.NewTarget, []string{
		`“new.target“;`,
	}},
	{js.CallExpression, []string{
		`“a()“;`,
		`“let()“;`,
		`“super(123)“;`,
		`“aa[123]()“;`,
		`««aa()».bbb()»;`,
		`“«aa()»()“;`,
		`“«aa()»[123]()“;`,
		`“«super()»[123]()“;`,
		`“«let()»[123]()“;`,
		`“«super()»()“;`,
	}},
	{js.Arguments, []string{
		`aa“()“.bbb“()““()“;`,
		`aa“(1,2)“;`,
	}},
	{js.PostfixExpression, []string{
	// TODO
	}},
	{js.UnaryExpression, []string{
	// TODO
	}},
	{js.MultiplicativeExpression, []string{
		`/*no expectations*/ 1;`,
		`“1*2“;`,
		// TODO `1+“2/3“;`,
		`“-1%~2“ << 3;`,
	}},
	{js.MultiplicativeOperator, []string{
		`/*no expectations*/ 1;`,
		`1“*“2;`,
		`1+2“%“3;`,
		// TODO `1“/“2;`,
	}},
	{js.AdditiveExpression, []string{
		`/*no expectations*/ 1;`,
		`“1+2“;`,
		`“1+2*3“;`,
		`“1-2“ << 3;`,
	}},
	{js.ShiftExpression, []string{
		`/*no expectations*/ 1;`,
		`“1<<2“;`,
		`“1>>2+3“;`,
		`“1>>>1“ < 3;`,
	}},
	{js.RelationalExpression, []string{
		`/*no expectations*/ 1;`,
		`“1 in {}“;`,
		`“1<2*4“;`,
		`“1>2“ != true;`,
		`if (“1 >= 1“ && “1 <= 1“ && “a instanceof Window“) alert('true');`,
	}},
	{js.EqualityExpression, []string{
	// TODO
	}},
	{js.BitwiseANDExpression, []string{
	// TODO
	}},
	{js.BitwiseXORExpression, []string{
	// TODO
	}},
	{js.BitwiseORExpression, []string{
	// TODO
	}},
	{js.LogicalANDExpression, []string{
	// TODO
	}},
	{js.LogicalORExpression, []string{
	// TODO
	}},
	{js.ConditionalExpression, []string{
	// TODO
	}},
	{js.AssignmentExpression, []string{
	// TODO
	}},
	{js.AssignmentOperator, []string{
	// TODO
	}},
	{js.Block, []string{
	// TODO
	}},
	{js.LexicalDeclaration, []string{
		`“let a;“`,
		`“let a = 5;“`,
		`“let [a] = [5];“`,
		`“const {b: [c]} = a;“`,
	}},
	{js.LexicalBinding, []string{
		`let “a“;`,
		`let “a = 5“;`,
		`let “[a] = [5]“;`,
		`const “{b: [c]} = a“;`,
	}},
	{js.VariableStatement, []string{
	// TODO
	}},
	{js.VariableDeclaration, []string{
	// TODO
	}},
	{js.ObjectBindingPattern, []string{
		`const “{b: [c]}“ = a;`,
		`const “{}“ = a;`,
		`for (const “{b,}“ in a);`,
	}},
	{js.ArrayBindingPattern, []string{
	// TODO
	}},
	{js.BindingElisionElement, []string{
	// TODO
	}},
	{js.BindingProperty, []string{
	// TODO
	}},
	{js.BindingElement, []string{
	// TODO
	}},
	{js.SingleNameBinding, []string{
	// TODO
	}},
	{js.BindingRestElement, []string{
	// TODO
	}},
	{js.EmptyStatement, []string{
		`“;“`,
		`for(;;)“;“`,
		`while(true)“;“`,
		`do“;“ while(true);`,
	}},
	{js.ExpressionStatement, []string{
		`“1+2;“ “v();“`,
	}},
	{js.IfStatement, []string{
		`“if (a in b); else continue;“`,
		`“if (a in b); else «if (true) a(); else b();»“`,
		`“if (a in b) {«if(a);»} else continue;“`,
	}},
	{js.DoWhileStatement, []string{
		`“do {} while(a < 5);“`,
		`“do; while(a < 5);“`,
	}},
	{js.WhileStatement, []string{
		`“while(false in a);“`,
	}},
	{js.ForStatement, []string{
		`“for (a=0; a < 5; a++);“`,
		`“for (var a; a < 5; );“`,
		`“for (var {a,b} = c; a < 5;);“`,
	}},
	{js.ForInStatement, []string{
		`“for (a in b) continue;“`,
		`“for (let [a] in b);“`,
		`“for (let.a in b);“`,
		`“for (let a in b);“`,
		`“for (let in b);“`,
		`“for (var a in b);“`,
		`“for (var [a] in b);“`,
	}},
	{js.ForOfStatement, []string{
		`“for (a of b);“`,
		`“for (var {name:[name]} of b);“`,
		`“for (const [[name]] of b);“`,
	}},
	{js.ForBinding, []string{
		`for (var “a“ in b);`,
		`for (var “[...a]“ in b);`,
		`for (const “{}“ in b);`,
		`for (let “[a]“ in b);`,
		`for (const “[,,,,...a]“ in b);`,
	}},
	{js.ContinueStatement, []string{
		`for(;;){ “continue;“ }`,
		`A: do “continue A;“ while(false);`,
	}},
	{js.BreakStatement, []string{
		`for(;;){ “break;“ }`,
		`A: do “break A;“ while(false);`,
	}},
	{js.ReturnStatement, []string{
		`function a() { “return 1;“ }`,
		`function b() { “return;“ }`,
	}},
	{js.WithStatement, []string{
		`“with(window) aa();“ {}`,
		`“with(window) { addListener(); }“ {}`,
	}},
	{js.SwitchStatement, []string{
		`“switch(a) {}“`,
		`“switch(a) {case 1: case 2: default: case 3:}“`,
		`“switch(a) {default: 1; case 3:}“`,
		`“switch(a) {case 3: {} default: 1;}“`,
	}},
	{js.CaseBlock, []string{
		`switch(a) “{}“`,
		`switch(a) “{case 1: case 2: default: case 3:}“`,
	}},
	{js.CaseClause, []string{
		`switch(a) {“case 1: a();“}`,
		`switch(a) {“case 1:“ “case 2:“ default: “case 3:“}`,
	}},
	{js.DefaultClause, []string{
		`switch(a) {case 1: case 2: “default:“ case 3:}`,
		`function a() { switch(a) {“default: return;“ case 3:} }`,
	}},
	{js.LabelledStatement, []string{
		`“yield: do break yield; while(false);“`,
		`“A: for(;;) { break A; }“`,
		`“A: function q() { return; }“`,
	}},
	{js.ThrowStatement, []string{
	// TODO
	}},
	{js.TryStatement, []string{
	// TODO
	}},
	{js.Catch, []string{
	// TODO
	}},
	{js.Finally, []string{
	// TODO
	}},
	{js.CatchParameter, []string{
	// TODO
	}},
	{js.DebuggerStatement, []string{
		`“debugger;“`,
	}},
	{js.FunctionDeclaration, []string{
		`“function id() { yield = 1; }“`,
		`export default “function() { yield = 1; }“`,
		`“function sum(a,b) { return a + b; }“`,
	}},
	{js.FunctionExpression, []string{
		`(“function() { yield = 1; }“)();`,
		`(“function id() { yield = 1; }“)();`,
		`(“function yield() { a++; }“)();`,
		`(“function as() { a++; }“)();`,
		`(“function let() { a++; }“)();`,
	}},
	{js.FormalParameters, []string{
		`(function(“a,b,c“) { yield = 1; })();`,
		`(function(““) { yield = 1; })();`,
		`export default function(““) { yield = 1; };`,
		`function q(“...a“) {}`,
	}},
	{js.FunctionRestParameter, []string{
		`function q(“...a“) {}`,
		`function q(b,c, “...a“) {}`,
	}},
	{js.FormalParameter, []string{
		`function q(“a“, “b“) {}`,
		`function q(“[id]“, “{name: name}“) {}`,
	}},
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
	{js.MethodDefinition, []string{
		`({ “run(){ console.log('executed'); }“}).run();`,
		`class A { “noop(input) {}“}`,
		`class A { “get x() { return this.x; }“ “set x(val) { this.x = val; }“}`,
	}},
	{js.GeneratorMethod, []string{
		`({“*gen(){ yield 1; yield 2; }“}).run();`,
	}},
	{js.GeneratorDeclaration, []string{
		`“function *gen(){ yield 1; yield 2; }“ {}`,
		`export default “function*(){ yield 1; yield 2; }“ {}`,
	}},
	{js.GeneratorExpression, []string{
		`(“function*(){ yield 1; yield 2; }“)();`,
		`(“function* a(){ yield 1; yield 2; }“)();`,
	}},
	{js.YieldExpression, []string{
		`function *gen(){ “yield 1“; “yield“; “yield *2“; } {}`,
	}},
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
	{js.ClassHeritage, []string{
		`class A “extends B“ {; ;}`,
		`class A “extends compose(B,C)“ {}`,
		`(class “extends (A)“ {});`,
	}},
	{js.ClassBody, []string{
	// TODO
	}},
	{js.ClassElement, []string{
	// TODO
	}},
	{js.Module, []string{
	// TODO
	}},
	{js.ModuleBody, []string{
	// TODO
	}},
	{js.ModuleItem, []string{
	// TODO
	}},
	{js.ImportDeclaration, []string{
	// TODO
	}},
	{js.ImportClause, []string{
	// TODO
	}},
	{js.ImportedDefaultBinding, []string{
	// TODO
	}},
	{js.NameSpaceImport, []string{
	// TODO
	}},
	{js.NamedImports, []string{
	// TODO
	}},
	{js.FromClause, []string{
	// TODO
	}},
	{js.ImportSpecifier, []string{
	// TODO
	}},
	{js.ModuleSpecifier, []string{
	// TODO
	}},
	{js.ImportedBinding, []string{
	// TODO
	}},
	{js.ExportDeclaration, []string{
	// TODO
	}},
	{js.ExportClause, []string{
	// TODO
	}},
	{js.ExportSpecifier, []string{
	// TODO
	}},

	// Automatic Semicolon Insertion
	{js.ExpressionStatement, []string{
		`{ “1“
     “2“ } “3“`,
	}},
	{js.InsertedSemicolon, []string{
		`{ 1«»
     2«» } 3«»`,
		` function a(){ return«»
     a + b«»
      a«»}`,
		`continue«»
     A«»`,
		`/*no expectations*/ for (
     ;;) {}`,
		`/*no expectations*/ for (;
     true;) {}`,
		`a = b«»
     ++c«»`,
		`a = b + c   /* not here */
     (d + e).print()«»`,
	}},
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
		e.t.Errorf("Unexpected %v: `%s` in `%s`", nt, e.source[offset:endoffset], e.source)
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
			res := p.Parse(l)
			if !res {
				t.Errorf("Parse() returned false for `%s`", source)
			} else {
				expTest.done()
			}
		}
	}
}
