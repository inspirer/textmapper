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
		"`ab${ “this“ }${ “this“ }c`",
	}},
	{js.RegularExpression, []string{
		`“/abc/i“.test('aaa')`,
		`1 / ++“/^[a-z]+\//ii“.lastIndex`,
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
		"print “`abc`“",
		"“`ab${ expr }${ expr2 }c`“",
	}},
	{js.TaggedTemplate, []string{
		"“tpl`ab${ expr }${ expr2 }c`“",
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
	{js.PostIncrementExpression, []string{
		`a(“b++“, c)`,
	}},
	{js.PostDecrementExpression, []string{
		`a(“b--“, c)`,
	}},
	{js.PreIncrementExpression, []string{
		`a(“++b“, c)`,
	}},
	{js.PreDecrementExpression, []string{
		`a(“--b“, c)`,
	}},
	{js.UnaryExpression, []string{
		`“delete a“
		 “void 1“
		 “typeof s“ == "string"
		 a = b
		 ++c;
		 “+1“ + “-2“ & “~0“
		 if (“!(«typeof s» === "string")“) {}`,
	}},
	{js.ExponentiationExpression, []string{
		`/*no expectations*/ 1;`,
		`“1**2“;`,
		`“1**«2**3»“;`,
		`“1**«2**--a»“;`,
		`“--1**«2**a++»“;`,
	}},
	{js.MultiplicativeExpression, []string{
		`/*no expectations*/ 1;`,
		`“1*2“;`,
		`1+“2/3“;`,
		`“-1%~2“ << 3;`,
	}},
	{js.AdditiveExpression, []string{
		`/*no expectations*/ 1;`,
		`“1+2“;`,
		`a ^ “1+2*3“;`,
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
		`if (“a === b“);`,
		`(“c !== 1+1“)`,
		`(“c != 1/2“)`,
		`(“a == 1“)`,
		// Left associative.
		`(“«a == 5» === true“)`,
	}},
	{js.BitwiseANDExpression, []string{
		`(“a&1“)`,
		`(“a==7 & 3+3“)`,
		// Left associative.
		`(“«a&7» & 3“)`,
	}},
	{js.BitwiseXORExpression, []string{
		`(“a^1“)`,
		`(“a&7 ^ 3“)`,
		`(“7 ^ a==3“)`,
		// Left associative.
		`(“«a^7» ^ 4“)`,
	}},
	{js.BitwiseORExpression, []string{
		`(“a|1“)`,
		`(“a&7 | 1“)`,
		`(a & (“7|1“))`,
		// Left associative.
		`(“«a|7» | 1“)`,
	}},
	{js.LogicalANDExpression, []string{
		`(“a && true“)`,
		`(“a && a==5“)`,
		// Left associative.
		`(“«a && b» && c“)`,
	}},
	{js.LogicalORExpression, []string{
		`(“a || true“)`,
		// Left associative.
		`(“«a || b» || c“)`,
	}},
	{js.ConditionalExpression, []string{
		`(“a ? b : c“)`,
		`(“a ? «b1 ? b : c» : «b2 ? b : c»“)`,
	}},
	{js.AssignmentExpression, []string{
		`{ “a = 5“ }`,
		`{ “a ^= 5“ }`,
		`{ “a >>>= 5“ }`,
		`{ “a |= 5“ }`,
		`{ “a **= 5“ }`,
	}},
	{js.AssignmentOperator, []string{
		` a“+=“1`,
		` a“**=“2`,
		` a“<<=“ b “+=“ 1`,
	}},
	{js.Block, []string{
		`“{}“`,
		`“{  «{1+2}»  ({a:b}) }“`,
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
		`“var a;“`,
		`“var a, b;“`,
		`“var a = 5, b;“`,
		`“var [a, b, c] = x, b;“`,
		`“var q, {names: [name1, name2, ...others],} = param“`,
	}},
	{js.VariableDeclaration, []string{
		`var “q“, “{obj1: {a,b,c}} = param“`,
	}},
	{js.ObjectBindingPattern, []string{
		`const “{b: [c]}“ = a;`,
		`const “{}“ = a;`,
		`const “{name}“ = a;`,
		`for (const “{b,}“ in a);`,
	}},
	{js.BindingProperty, []string{
		`const {“name“, “a: {}“} = a;`,
	}},
	{js.SingleNameBinding, []string{
		`const {“name“} = a;`,
		`let [“name“] = a;`,
	}},
	{js.ArrayBindingPattern, []string{
		`var “[]“ = x;`,
		`let “[x]“ = y;`,
		`let “[x, ...rest]“ = y;`,
	}},
	{js.BindingElisionElement, []string{
		`let [“x“, “y“] = y;`,
		`let [“x“, ...rest] = y;`,
	}},
	{js.BindingRestElement, []string{
		`let [x, “...rest“] = y;`,
		`let [“...oth“] = y;`,
	}},
	{js.BindingElement, []string{
		`let [“oth“, “[«A»]“, “{a}“] = y;`,
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
		`function a() { “throw new Error('oops!')“ }`,
		`function a() { “throw a(1)“ }`,
	}},
	{js.TryStatement, []string{
		`“try {
       call();
     } catch (e) {
			 if (e instanceof AError) {
				 console.log('AError');
			 } else {
				 throw e
			 }
		 }“`,
	}},
	{js.Catch, []string{
		`try {} “catch (e) {
		   throw e
		 }“`,
	}},
	{js.Finally, []string{
		`try {} catch (e) {
		   throw e
		 } “finally {log.console('done')}“`,
		`try {
		   call();
		 } “finally {
		   log.console('done')
		 }“`,
	}},
	{js.CatchParameter, []string{
		`try {} catch (“e“) {
		   throw e
		 }`,
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
		`class A extends B “{; ;}“`,
		`class A extends B “{
		   ;
		   a() { return 1}
		 }“`,
	}},
	{js.ClassElement, []string{
		`class A extends B {
		   “;“
		   “a() { return 1}“
		   “*a() { yield 1; yield 2}“
		   “get x() { return this.x}“
		   “set x(val) { this.x = val}“
		 }`,
	}},
	{js.Module, []string{
		`““`,
		`
		““`,
		` “a = 4“ `,
	}},
	{js.ModuleItem, []string{
		` “a = 4;“ “b=5“
		  “function a(){}“`,
	}},
	{js.ImportDeclaration, []string{
		`“import './aaa'“`,
		`“import * as aaa from './aaa'“`,
		`“import {b,c,} from './aaa'“`,
		`“import aaa from './aaa'“`,
		`“import aaa, {t} from './aaa'“`,
		`“import aaa, * as oth from './aaa'“`,
	}},
	{js.NameSpaceImport, []string{
		`import “* as aaa“ from './aaa'`,
		`import aaa, “* as oth“ from './aaa'`,
	}},
	{js.NamedImports, []string{
		`import “{}“ from './aaa'`,
		`import aaa, “{t as b}“ from './aaa'`,
	}},
	{js.ImportSpecifier, []string{
		`import {“q“,“o“,} from './aaa'`,
		`import aaa, {“t as b“} from './aaa'`,
	}},
	{js.ModuleSpecifier, []string{
		`import “'./aaa'“`,
		`import * as aaa from “'./aaa'“`,
	}},
	{js.ExportDeclaration, []string{
		`“export * from "aa/bb"“`,
		`“export {} from "aa/bb"“`,
		`“export {a, b as c} from "aa/bb"“`,
		`“export {q, t, }“`,
		`“export var v = 5“`,
		`“export const a = 1.2345“`,
		`“export function sum (x, y) { return x + y }“`,
		`“export class A {}“`,
	}},
	{js.ExportDefault, []string{
		`“export default (x) => x*x“`,
		`“export default class {}“`,
		`“export default function(x, y) { return x + y }“`,
		`“export default function*(x, y) { yield
		                                    yield z
		                                   yield y}“`,
		`“export default 5“`,
		`“export default a = 5“`,
	}},
	{js.ExportClause, []string{
		`export “{}“ from "aa/bb"`,
		`export “{q,}“ from "aa/bb"`,
		`export “{q as p, c}“ from "aa/bb"`,
	}},
	{js.ExportSpecifier, []string{
		`export {“a as b“, “c“, }`,
	}},

	// Automatic Semicolon Insertion
	{js.InsertedSemicolon, []string{
		/* at EOI, before '}', and after ')' */
		`var a«»
		`,
		`{ 1«»
     2«» } 3«»`,
		`do; while(true)«» 1+2;`,
		`do; while(true)«» function a(){}`,
		`{ do; while(true)«» }`,

		`{1«»} (1+2)«»`,
		`{1«»} (1+2)«»
		  3«»`,
		`{1«»} (1+2) 3 /*fails*/`,

		/* 'for' semicolons are not insertable */
		`/*no expectations*/ for (
     ;;) {}`,
		`/*no expectations*/ for (;
     true;) {}`,
		`/*no expectations*/ for (;;
     ) {}`,
		`/*no expectations*/ for (a; b
		) /*fails*/`,

		/* 'empty statement' semicolons are not insertable */
		`/*no expectations*/ if (true) /*fails*/`,
		`if (a > b);
     else c = d«»`,
		`/*no expectations*/ if (a > b)
     else c = d /*fails*/`,

		/* Can parse without a semicolon */
		`a = b + c   /* not here */
     (d + e).print()«»`,
		`++c
		 +1«»`,

		/* restricted productions: ArrowFunction */
		`a = b=>b+1«»`,
		`a = b«»
		  =>b+1 /*fails*/`,

		/* restricted productions: Yield */
		`function *a() { yield«» }`,
		`function *a() { yield a+b«»}`,
		`function *a() { yield«»
		                 a+b«»}`,
		`function *a() { yield«»
		                 *l} /*fails*/`,

		/* restricted productions: PostfixExpression */
		`a = b«»
     ++c«»`,
		`a = b«»
     --«» /*fails*/`,

		/* restricted productions: ReturnStatement */
		` function a(){ return«» }`,
		` function a(){
		    return«»
		  }`,
		` function a(){ return a + b«» }`,
		` function a(){ return«»
                    a + b«»
      a«»}`,

		/* restricted productions: ContinueStatement */
		`continue A«»`,
		`continue«»
     A«»`,

		/* restricted productions: BreakStatement */
		`while(true) break A«»`,
		`for(;
		     ;) break«»
     A«»`,

		/* restricted productions: ThrowStatement */
		`throw A«»`,
		`throw«»
     A /*fails*/`,
	}},

	{js.JSXElement, []string{
		// TODO
	}},
	{js.JSXSelfClosingElement, []string{
		// TODO
	}},
	{js.JSXOpeningElement, []string{
		// TODO
	}},
	{js.JSXClosingElement, []string{
		// TODO
	}},
	{js.JSXElementName, []string{
		// TODO
	}},
	{js.JSXAttribute, []string{
		// TODO
	}},
	{js.JSXSpreadAttribute, []string{
		// TODO
	}},
	{js.JSXAttributeName, []string{
		// TODO
	}},
	{js.JSXAttributeValue, []string{
		// TODO
	}},
	{js.JSXText, []string{
		// TODO
	}},
	{js.JSXChild, []string{
		// TODO
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

type node struct {
	offset, endoffset int
}

type expTest struct {
	source       []byte
	expectedType js.NodeType
	exp          []jsTestExpectation
	t            *testing.T
	parsed       []node
}

func (e *expTest) Node(nt js.NodeType, offset, endoffset int) {
	e.parsed = append(e.parsed, node{offset, endoffset})
	if e.expectedType != nt {
		return //len(e.parsed)
	}
	if len(e.exp) == 0 {
		e.t.Errorf("Unexpected %v: `%s` in `%s`", nt, e.source[offset:endoffset], e.source)
	} else if e.exp[0].offset != offset || e.exp[0].endoffset != endoffset {
		first := e.exp[0]
		e.t.Errorf("got `%s`, want `%s`", e.source[offset:endoffset], e.source[first.offset:first.endoffset])
	} else {
		e.exp = e.exp[1:]
	}
	return //len(e.parsed)
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

	seen := map[js.NodeType]bool{}
	for _, tc := range jsParseTests {
		seen[tc.nt] = true
		for _, input := range tc.inputs {
			source, exp := splitInput(input, t)
			if len(exp) == 0 && !strings.HasPrefix(input, "/*no expectations*/") {
				t.Errorf("No expectations in `%s`", input)
			}
			expected := !strings.HasSuffix(input, "/*fails*/")

			onError := func(line, offset, len int, msg string) {
				if expected {
					t.Errorf("%d, %d: %s", line, offset, msg)
				}
			}
			expTest := &expTest{source, tc.nt, exp, t, nil}

			l.Init([]byte(source), onError)
			p.Init(onError, expTest)
			res := p.Parse(l)
			if res != expected {
				t.Errorf("Parse() returned %v for `%s`", res, source)
			} else {
				expTest.done()
			}
		}
	}
	for n := js.NodeType(1); n < js.NodeTypeMax; n++ {
		if !seen[n] {
			t.Errorf("%v is not tested", n)
		}
	}
}

type consumer struct{}

func (c consumer) Node(t js.NodeType, offset, endoffset int) {
}

func BenchmarkParser(b *testing.B) {
	l := new(js.Lexer)
	p := new(js.Parser)
	onError := func(line, offset, len int, msg string) {
		b.Errorf("%d, %d: %s", line, offset, msg)
	}

	p.Init(onError, consumer{})
	code := []byte(jsBenchmarkCode)
	for i := 0; i < b.N; i++ {
		l.Init(code, onError)
		p.Parse(l)
	}
	b.SetBytes(int64(len(jsBenchmarkCode)))
}
