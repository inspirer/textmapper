package js_test

import (
	"testing"

	"github.com/inspirer/textmapper/tm-parsers/js"
	"github.com/inspirer/textmapper/tm-parsers/parsertest"
)

var parseTests = []struct {
	nt     js.NodeType
	inputs []string
}{

	{js.SingleLineComment, []string{
		` «// abc»
		  «// abc2»
		  var i = 1;     «// 8»
		  «// abc2»`,
	}},
	{js.MultiLineComment, []string{
		`const a «/* te ** / st */» = 5;`,
		`var a = 5;  «/* abc */»`,
	}},
	{js.IdentifierReference, []string{
		`/*no expectations*/ const a = 15;`,
		`/*no expectations*/ var b = 7;`,
		`var e = «aa»;`,
		`var e = «let»;`,
		`for (; «b» < «a»; «b»++) { };`,
		`/*no expectations*/ var c = (function() {})();`,

		// V8 runtime functions.
		`let a = «%StringBuilderConcat»(«parts», «len» + 1, "")`,

		// IdentifierName rules
		`const a = {cc: 5}.«cc»;`,
		`import {«a» as b} from './Test1';`,
		`export {«a», «b» as c} from "aa/bb"`,

		// async, yield, of
		`for («async» of [1,2,3]) { «console».«log»(«async»); }`,
		`for («yield» of [1,2,3]) { «console».«log»(«yield»); }`,
		`for(var a /*binding*/ = (async/*kw*/ of/*binding*/=>5)(); «a» != null; «a» = null) {
		    «a».«then»(of/*binding*/=>«console».«log»(«of»)); 0;
		 }`,
		`/*no expectations*/ for(async of=>5;;) {}`,
	}},
	{js.BindingIdentifier, []string{
		`const «a» = 15;`,
		`var «b» = 7;`,
		`var «e» = aa;`,
		`/*no expectations*/ for (; b < a; b++) { }`,
		`var «c» = (function() {})();`,
		`import {a as «b»} from './Test1';`,

		// IdentifierName rules
		`const «a» = {«cc»: 5}.cc;`,
		`export {a, b as «c»} from "aa/bb"`,

		// Catch parameter
		`try {} catch («e») {
		   throw e
		 }`,
	}},
	{js.LabelIdentifier, []string{
		`«A»: for (;;) {continue «A»; }`,
		`break «B»;`,
	}},
	{js.This, []string{
		`const c = «this».e;`,
		"`ab${ «this» }${ «this» }c`",
	}},
	{js.Regexp, []string{
		`«/abc/i».test('aaa')`,
		`1 / ++«/^[a-z]+\//ii».lastIndex`,
	}},
	{js.Parenthesized, []string{
		`c = «(1+2)»;`,
	}},
	{js.Literal, []string{
		`c = «1» | a[«2»] | «true» | «false» | «1e4»;`,

		// 055 is parsed as an octal literal in 055.5
		`«055»§.5`,
		`«059.5»`,
		`«09.9»`,
		`«011»`,
	}},
	{js.ArrayLiteral, []string{
		`c = «[4]».concat(«[1, 2, 3]»);`,
	}},
	{js.SpreadElement, []string{
		`c = [4,,,, «...a»];`,
	}},
	{js.ObjectLiteral, []string{
		`(«{}»);`,
		`a = «{}»;`,
		`for (let {} of «{}»);`,
		`a = «{a: [], c: 1}»;`,
		`a = «{a: «{}»,}»;`,
	}},
	{js.ShorthandProperty, []string{
		`{} ({«a»,b: 1 in {}, f() {}, });`,
	}},
	{js.Property, []string{
		`{} ({a,«b: 1 in {}», f() {}, });`,
	}},
	{js.LiteralPropertyName, []string{
		`{} ({a,«b»: 1 in {}, «f»() {}, get «d»() { return 1;}});`,
		`{} ({* «d»() { yield 1;}});`,
	}},
	{js.ComputedPropertyName, []string{
		`{} ({* «["a"+i]»() { yield 1;}});`,
	}},
	{js.Initializer, []string{
		`({a «= b», yield «= {yield}»});`,
		`var a «= 4», b «=[]»;`,
	}},
	{js.TemplateLiteral, []string{
		"print «`abc`»",
		"«`ab${ expr }${ expr2 }c`»",
	}},
	{js.NoSubstitutionTemplate, []string{
		"print «`abc`»",
	}},
	{js.TemplateHead, []string{
		"print «`ab${»123}c`",
	}},
	{js.TemplateMiddle, []string{
		"/*no expectations*/ print `a${123}bc`",
		"print `a${123«}${»234}bc`",
		"print `a${123«}abc${»234}bc`",
		"print `a${12«}abc1${»3«}abc2${»234}bc`",
	}},
	{js.TemplateTail, []string{
		"print `a${123«}bc`»",
	}},
	{js.TaggedTemplate, []string{
		"«tpl`ab${ expr }${ expr2 }c`»",
	}},
	{js.IndexAccess, []string{
		`«super[10]»();`,
		`«super()[10]»();`,
		`«s.a()[10]»();`,
		`««s.a()[10]»[10]»();`,
	}},
	{js.PropertyAccess, []string{
		`for (««let.a».b» in b);`,
		`«a.b»();`,
		`«a().b»();`,
		`«a()[10].b»();`,
		`«super.me»();`,
	}},
	{js.SuperExpression, []string{
		`«super».me();`,
	}},
	{js.NewExpression, []string{
		`«new A»;`,
		`«new A.b[10]»;`,
		`«new A()»;`,
		`«new A.b[10]()».a();`,
		`«new A.b[10]()»[10];`,
	}},
	{js.NewTarget, []string{
		`«new.target»;`,
	}},
	{js.CallExpression, []string{
		`«a()»;`,
		`«let()»;`,
		`«super(123)»;`,
		`«aa[123]()»;`,
		`««aa()».bbb()»;`,
		`««aa()»()»;`,
		`««aa()»[123]()»;`,
		`««super()»[123]()»;`,
		`««let()»[123]()»;`,
		`««super()»()»;`,
	}},
	{js.Arguments, []string{
		`aa«()».bbb«()»«()»;`,
		`aa«(1,2)»;`,
	}},
	{js.PostInc, []string{
		`a(«b++», c)`,
	}},
	{js.PostDec, []string{
		`a(«b--», c)`,
	}},
	{js.PreInc, []string{
		`a(«++b», c)`,
	}},
	{js.PreDec, []string{
		`a(«--b», c)`,
	}},
	{js.UnaryExpression, []string{
		`«delete a»
		 «void 1»
		 «typeof s» == "string"
		 a = b
		 ++c;
		 «+1» + «-2» & «~0»
		 if («!(«typeof s» === "string")») {}`,
	}},
	{js.ExponentiationExpression, []string{
		`/*no expectations*/ 1;`,
		`«1**2»;`,
		`«1**«2**3»»;`,
		`«1**«2**--a»»;`,
		`«--1**«2**a++»»;`,
	}},
	{js.MultiplicativeExpression, []string{
		`/*no expectations*/ 1;`,
		`«1*2»;`,
		`1+«2/3»;`,
		`«-1%~2» << 3;`,
	}},
	{js.AdditiveExpression, []string{
		`/*no expectations*/ 1;`,
		`«1+2»;`,
		`a ^ «1+2*3»;`,
		`«1-2» << 3;`,
	}},
	{js.ShiftExpression, []string{
		`/*no expectations*/ 1;`,
		`«1<<2»;`,
		`«1>>2+3»;`,
		`«1>>>1» < 3;`,
	}},
	{js.RelationalExpression, []string{
		`/*no expectations*/ 1;`,
		`«1 in {}»;`,
		`«1<2*4»;`,
		`«1>2» != true;`,
		`if («1 >= 1» && «1 <= 1» && «a instanceof Window») alert('true');`,
	}},
	{js.EqualityExpression, []string{
		`if («a === b»);`,
		`(«c !== 1+1»)`,
		`(«c != 1/2»)`,
		`(«a == 1»)`,
		// Left associative.
		`(««a == 5» === true»)`,
	}},
	{js.BitwiseANDExpression, []string{
		`(«a&1»)`,
		`(«a==7 & 3+3»)`,
		// Left associative.
		`(««a&7» & 3»)`,
	}},
	{js.BitwiseXORExpression, []string{
		`(«a^1»)`,
		`(«a&7 ^ 3»)`,
		`(«7 ^ a==3»)`,
		// Left associative.
		`(««a^7» ^ 4»)`,
	}},
	{js.BitwiseORExpression, []string{
		`(«a|1»)`,
		`(«a&7 | 1»)`,
		`(a & («7|1»))`,
		// Left associative.
		`(««a|7» | 1»)`,
	}},
	{js.LogicalANDExpression, []string{
		`(«a && true»)`,
		`(«a && a==5»)`,
		// Left associative.
		`(««a && b» && c»)`,
	}},
	{js.LogicalORExpression, []string{
		`(«a || true»)`,
		// Left associative.
		`(««a || b» || c»)`,
	}},
	{js.ConditionalExpression, []string{
		`(«a ? b : c»)`,
		`(«a ? «b1 ? b : c» : «b2 ? b : c»»)`,
	}},
	{js.AssignmentExpression, []string{
		`{ «a = 5» }`,
		`{ «a ^= 5» }`,
		`{ «a >>>= 5» }`,
		`{ «a |= 5» }`,
		`{ «a **= 5» }`,
	}},
	{js.CommaExpression, []string{
		`{ «a = 5, b = 6» }`,
		`{ a = («5,  6») }`,
		`{ a((«5, 6»), 9) }`,
	}},
	{js.AssignmentOperator, []string{
		` a«+=»1`,
		` a«**=»2`,
		` a«<<=» b «+=» 1`,
	}},
	{js.Block, []string{
		`«{}»`,
		`«{  «{1+2}»  ({a:b}) }»`,
		`switch(a) «{}»`,
		`switch(a) «{case 1: case 2: default: case 3:}»`,
	}},
	{js.LexicalDeclaration, []string{
		`«let a;»`,
		`«let a = 5;»`,
		`«let [a] = [5];»`,
		`«const {b: [c]} = a;»`,
	}},
	{js.LexicalBinding, []string{
		`let «a»;`,
		`let «a = 5»;`,
		`let «[a] = [5]»;`,
		`const «{b: [c]} = a»;`,
	}},
	{js.VariableStatement, []string{
		`«var a;»`,
		`«var a, b;»`,
		`«var a = 5, b;»`,
		`«var [a, b, c] = x, b;»`,
		`«var q, {names: [name1, name2, ...others],} = param»`,
	}},
	{js.VariableDeclaration, []string{
		`var «q», «{obj1: {a,b,c}} = param»`,
	}},
	{js.ObjectPattern, []string{
		`const «{b: [c]}» = a;`,
		`const «{}» = a;`,
		`const «{name}» = a;`,
		`for (const «{b,}» in a);`,
	}},
	{js.PropertyBinding, []string{
		`const {name, «a: {}»} = a;`,
	}},
	{js.ElementBinding, []string{
		`let [oth, «[A]», «{a}»] = y;`,
	}},
	{js.SingleNameBinding, []string{
		`const {«name»} = a;`,
		`let [«name»] = a;`,
		`let [«oth», [«A»], {«a»}] = y;`,
	}},
	{js.ArrayPattern, []string{
		`var «[]» = x;`,
		`let «[x]» = y;`,
		`let «[x, ...rest]» = y;`,
	}},
	{js.BindingRestElement, []string{
		`let [x, «...rest»] = y;`,
		`let [«...oth»] = y;`,
	}},
	{js.EmptyStatement, []string{
		`«;»`,
		`for(;;)«;»`,
		`while(true)«;»`,
		`do«;» while(true);`,
	}},
	{js.ExpressionStatement, []string{
		`«1+2;» «v();»`,
		`function a() {
			«yield»
			«yield()»
		}`,
	}},
	{js.IfStatement, []string{
		`«if (a in b); else continue;»`,
		`«if (a in b); else «if (true) a(); else b();»»`,
		`«if (a in b) {«if(a);»} else continue;»`,
	}},
	{js.DoWhileStatement, []string{
		`«do {} while(a < 5);»`,
		`«do; while(a < 5);»`,
	}},
	{js.WhileStatement, []string{
		`«while(false in a);»`,
	}},
	{js.ForStatement, []string{
		`«for (a=0; a < 5; a++);»`,
	}},
	{js.ForStatementWithVar, []string{
		`«for (var a; a < 5; );»`,
		`«for (var {a,b} = c; a < 5;);»`,
	}},
	{js.ForCondition, []string{
		`for (a=0; «a < 5»; a++);`,
		`for (var a; «a < 5»; );`,
		`for (var {a,b} = c; «a < 5»;); for (; «»;);`,
	}},
	{js.ForFinalExpression, []string{
		`for (a=0; a < 5; «a++»);`,
		`for (var a; a < 5; «c++, d++»); for (;; «»);`,
	}},
	{js.ForInStatement, []string{
		`«for (a in b) continue;»`,
		`«for (let.a in b);»`,
		`«for (let in b);»`,
	}},
	{js.ForInStatementWithVar, []string{
		`«for (let [a] in b);»`,
		`«for (let a in b);»`,
		`«for (var a in b);»`,
		`«for (var [a] in b);»`,
	}},
	{js.ForOfStatement, []string{
		`«for (a of b);»`,
	}},
	{js.ForOfStatementWithVar, []string{
		`«for (var {name:[name]} of b);»`,
		`«for (const [[name]] of b);»`,
	}},
	{js.ForBinding, []string{
		`for (var «a» in b);`,
		`for (var «[...a]» in b);`,
		`for (const «{}» in b);`,
		`for (let «[a]» in b);`,
		`for (const «[,,,,...a]» in b);`,
	}},
	{js.ContinueStatement, []string{
		`for(;;){ «continue;» }`,
		`A: do «continue A;» while(false);`,
	}},
	{js.BreakStatement, []string{
		`for(;;){ «break;» }`,
		`A: do «break A;» while(false);`,
	}},
	{js.ReturnStatement, []string{
		`function a() { «return 1;» }`,
		`function b() { «return;» }`,
	}},
	{js.WithStatement, []string{
		`«with(window) aa();» {}`,
		`«with(window) { addListener(); }» {}`,
	}},
	{js.SwitchStatement, []string{
		`«switch(a) {}»`,
		`«switch(a) {case 1: case 2: default: case 3:}»`,
		`«switch(a) {default: 1; case 3:}»`,
		`«switch(a) {case 3: {} default: 1;}»`,
	}},
	{js.Case, []string{
		`switch(a) {«case 1: a();»}`,
		`switch(a) {«case 1:» «case 2:» default: «case 3:»}`,
	}},
	{js.Default, []string{
		`switch(a) {case 1: case 2: «default:» case 3:}`,
		`function a() { switch(a) {«default: return;» case 3:} }`,
	}},
	{js.LabelledStatement, []string{
		`«yield: do break yield; while(false);»`,
		`«A: for(;;) { break A; }»`,
		`«A: function q() { return; }»`,
	}},
	{js.ThrowStatement, []string{
		`function a() { «throw new Error('oops!')» }`,
		`function a() { «throw a(1)» }`,
	}},
	{js.TryStatement, []string{
		`«try {
       call();
     } catch (e) {
			 if (e instanceof AError) {
				 console.log('AError');
			 } else {
				 throw e
			 }
		 }»`,
	}},
	{js.Catch, []string{
		`try {} «catch (e) {
		   throw e
		 }»`,
	}},
	{js.Finally, []string{
		`try {} catch (e) {
		   throw e
		 } «finally {log.console('done')}»`,
		`try {
		   call();
		 } «finally {
		   log.console('done')
		 }»`,
	}},
	{js.DebuggerStatement, []string{
		`«debugger;»`,
	}},
	{js.Function, []string{
		`«function id() { yield = 1; }»`,
		`export default «function() { yield = 1; }»`,
		`«function sum(a,b) { return a + b; }»`,
	}},
	{js.FunctionExpression, []string{
		`(«function() { yield = 1; }»)();`,
		`(«function id() { yield = 1; }»)();`,
		`(«function yield() { a++; }»)();`,
		`(«function as() { a++; }»)();`,
		`(«function let() { a++; }»)();`,
	}},
	{js.Parameters, []string{
		`(function«(a,b,c)» { yield = 1; })();`,
		`(function«()» { yield = 1; })();`,
		`export default function«()» { yield = 1; };`,
		`function q«(...a)» {}`,

		// in arrow functions
		`(«a» => a + 1)(1);`,
		`(«(a,b)» => { return a*b; })(1);`,
	}},
	{js.RestParameter, []string{
		`function q(«...a») {}`,
		`function q(b,c, «...a») {}`,
	}},
	{js.Parameter, []string{
		`function q(«a», «b») {}`,
		`function q(«[id]», «{name: name}») {}`,
	}},
	{js.ArrowFunction, []string{
		`(«a => a + 1»)(1);`,
		`(«(a,b) => { return a*b; }»)(1);`,
		`
		 var a = async
		 «v => 1»

		 var b = async
		 «(v, c) => 1»`,
	}},
	{js.ConciseBody, []string{
		`(a => «a + 1»)(1);`,
	}},
	{js.Getter, []string{
		`class A { «get x() { return this.x; }» set x(val) { this.x = val; }}`,
	}},
	{js.Setter, []string{
		`class A { get x() { return this.x; } «set x(val) { this.x = val; }»}`,
	}},
	{js.Method, []string{
		`({ «run(){ console.log('executed'); }»}).run();`,
		`class A { «noop(input) {}»}`,
	}},
	{js.GeneratorMethod, []string{
		`({«*gen(){ yield 1; yield 2; }»}).run();`,
	}},
	{js.Generator, []string{
		`«function *gen(){ yield 1; yield 2; }» {}`,
		`export default «function*(){ yield 1; yield 2; }» {}`,
	}},
	{js.GeneratorExpression, []string{
		`(«function*(){ yield 1; yield 2; }»)();`,
		`(«function* a(){ yield 1; yield 2; }»)();`,
	}},
	{js.Body, []string{
		`(function*()«{ yield 1; yield 2; }»)();`,
		`class A { noop(input) «{}»}`,
		`function a() «{if (false) a();}»`,
		`var a = function() «{if (false) a();}»;`,

		// in arrow functions
		`((a,b) => «{ return a*b; }»)(1);`,
	}},
	{js.Yield, []string{
		`function *gen(){
		   «yield»
		   a()
		   «yield»
		   +1
		   «yield +1»
		   «yield 1»; «yield»; «yield *2»;
		   function foo(yield) {
		     yield
		     return yield + 5
		   }
		   var f = v => yield + 1
		   «yield 22»
		 } {}`,
	}},
	{js.AsyncArrowFunction, []string{
		`/*no expectations*/ for(«async of=>5»;;) {}`,
		`/*no expectations*/ for(«async (of)=>5»;;) {}`,
		`/*no expectations*/ for(«async (of)=>{}»;;) {}`,
		`var f = «async v => await + 1»
		 await + 1
     ;
		 var a = async
		 v => 1
     ;
		 var a = «async v => 1»
     ;
		 async
		 v => 1
     ;
		 «async v => 1»
     ;
		 async
		 (v) => 1
     ;
		 «async (v) => 1»
		 `,
		`var f = «async v => await + 1»
		 await + 1`,
	}},
	{js.AsyncMethod, []string{
		`class A {
		  «async a() {}»
		  «async yield() {}»
		  «async await() {}»
		}`,
		`/*no expectations*/ class A {
		  async
		  §a() §{}
		}`,
	}},
	{js.AsyncFunction, []string{
		`«async function add2(x) {
       var a = await resolveAfter2Seconds(20);
       var b = await resolveAfter2Seconds(30);
       return x + a + b;
     }»

     async++
		 function nop() {}
		 a = async
		 function nop() {}
		 async;
		 function nop() {}
		 async /* no semicolon */
		 function nop() {}`,
	}},
	{js.AsyncFunctionExpression, []string{
		`var a = «async function(x) {
       var a = await resolveAfter2Seconds(20);
       var b = await resolveAfter2Seconds(30);
       return x + a + b;
     }»`,
		`var a = «async function(x) {
       var a = await resolveAfter2Seconds(20);
       var b = await resolveAfter2Seconds(30);
       return x + a + b;
     }»`,
		`this.it('is a test', «async function () {
       const foo = await 3;
       const bar = await new Promise(function (resolve) {
         resolve('7');
       });
       const baz = bar * foo;
       console.log(baz);
	   }»);`,
		`/*no expectations*/ var a = async
		 function(x) {}`,
	}},
	{js.AwaitExpression, []string{
		`async function gogo() {
       var b = «await func1(10)»;
       return b + 1;
     }`,
		`var f = async v => «await go()»
		 await + 1`,
	}},
	{js.Class, []string{
		`«class A {}»`,
		`«class A extends B {; ;}»`,
		`«class A extends B { a() {} }»`,
		`export default «class extends B {}»`,
		`export default «class { get a() { return 1;}}»`,
	}},
	{js.ClassExpr, []string{
		`(«class A {}»);`,
		`(«class A extends B {; ;}»);`,
		`(«class A extends B { a() {} }»);`,
		`(«class extends B {}»);`,
		`(«class { get a() { return 1;}}»);`,
	}},
	{js.Extends, []string{
		`class A «extends B» {; ;}`,
		`class A «extends compose(B,C)» {}`,
		`(class «extends (A)» {});`,
	}},
	{js.ClassBody, []string{
		`class A extends B «{; ;}»`,
		`class A extends B «{
		   ;
		   a() { return 1}
		 }»`,
	}},
	{js.EmptyDecl, []string{
		`class A extends B {
		   «;» «;»
		 }`,
	}},
	{js.StaticMethod, []string{
		`class A extends B {
		   ;
		   «static a() { return 1}»
		   *a() { yield 1; yield 2}
		   «static get x() { return this.x}»
		   set x(val) { this.x = val}
		 }`,
	}},
	{js.Module, []string{
		`«»`,
		`
		«»`,
		` «a = 4» `,
	}},
	{js.ImportDeclaration, []string{
		`«import './aaa'»`,
		`«import * as aaa from './aaa'»`,
		`«import {b,c,} from './aaa'»`,
		`«import aaa from './aaa'»`,
		`«import aaa, {t} from './aaa'»`,
		`«import aaa, * as oth from './aaa'»`,
	}},
	{js.NameSpaceImport, []string{
		`import «* as aaa» from './aaa'`,
		`import aaa, «* as oth» from './aaa'`,
	}},
	{js.NamedImports, []string{
		`import «{}» from './aaa'`,
		`import aaa, «{t as b}» from './aaa'`,
	}},
	{js.ImportSpecifier, []string{
		`import {«q»,«o»,} from './aaa'`,
		`import aaa, {«t as b»} from './aaa'`,
	}},
	{js.ModuleSpecifier, []string{
		`import «'./aaa'»`,
		`import * as aaa from «'./aaa'»`,
	}},
	{js.ExportDeclaration, []string{
		`«export * from "aa/bb"»`,
		`«export {} from "aa/bb"»`,
		`«export {a, b as c} from "aa/bb"»`,
		`«export {q, t, }»`,
		`«export var v = 5»`,
		`«export const a = 1.2345»`,
		`«export function sum (x, y) { return x + y }»`,
		`«export class A {}»`,
	}},
	{js.ExportDefault, []string{
		`«export default (x) => x*x»`,
		`«export default class {}»`,
		`«export default function(x, y) { return x + y }»`,
		`«export default function*(x, y) { yield
		                                    yield z
		                                   yield y}»`,
		`«export default 5»`,
		`«export default a = 5»`,
	}},
	{js.ExportClause, []string{
		`export «{}» from "aa/bb"`,
		`export «{q,}» from "aa/bb"`,
		`export «{q as p, c}» from "aa/bb"`,
	}},
	{js.ExportSpecifier, []string{
		`export {«a as b», «c», }`,
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
		`{1«»} (1+2) §3«»`, /* recovers */

		/* 'for' semicolons are not insertable */
		`/*no expectations*/ for (
     ;;) {}`,
		`/*no expectations*/ for (;
     true;) {}`,
		`/*no expectations*/ for (;;
     ) {}`,
		`/*no expectations*/ for (a; b
		§)«» `, /* recovers */

		/* 'empty statement' semicolons are not insertable */
		`/*no expectations*/ if (true) /*fails*/§`,
		`if (a > b);
     else c = d«»`,
		`if (a > b)
     §else c = d«»`, /* recovers */

		/* Can parse without a semicolon */
		`a = b + c   /* not here */
     (d + e).print()«»`,
		`++c
		 +1«»`,

		/* restricted productions: ArrowFunction */
		`a = b=>b+1«»`,
		`a = b«»
		  §=>b+1«»`, /* recovers */

		/* restricted productions: Yield */
		`function *a() { yield«» }`,
		`function *a() { yield a+b«»}`,
		`function *a() { yield«»
		                 a+b«»}`,
		`function *a() { yield«»
		                 §*l«» }`, /* recovers */

		/* restricted productions: PostfixExpression */
		`a = b«»
     ++c«»`,
		`a = b«»
     --«»§`, /* recovers */

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
     §A«»`, /* recovers */
	}},

	// JSX
	{js.JSXElement, []string{
		`var a = «<div>ABC</div>»;`,
		`var a = «<div/>»;`,
		`var a = «<Q>«<P/>»</Q>»;`,
		`var a = «<div>< / div>»;`,
		`var a = «<div / >»;`,
		`var a = «<q>{ «<a href={ {a: {b: 1}}.a.b }/>» }</q>»;`,
		`var a = «<q>{ [1,2,3].map(a => («<a href={a}/>»)) }</q>»;`,
		`var a = «<X comp=«<Y text=«<h1>Title</h1>» />» />»;`,
	}},
	{js.JSXSelfClosingElement, []string{
		`var a = «<div / >»;`,
	}},
	{js.JSXOpeningElement, []string{
		`var a = «<div name="a">»  </div>;`,
	}},
	{js.JSXClosingElement, []string{
		`var a = <div name="a">  «</div>»;`,
	}},
	{js.JSXElementName, []string{
		`var a = <«a.b.c.d» />;`,
		`var a = <«a:b» />;`,
		`var a = <«Q» />;`,
	}},
	{js.JSXNormalAttribute, []string{
		`var a = <div «name="a"»><div «name="b"»></div></div>;`,
	}},
	{js.JSXSpreadAttribute, []string{
		`var a = <div «{...props}»/>;`,
	}},
	{js.JSXAttributeName, []string{
		`var a = <div «name»="a"/>;`,
	}},
	{js.JSXText, []string{
		`var a = <A:A>« ABC »</A:A>;`,
		`var a = <A:A>«bb»{1}«cc»{<abc/>}</A:A>;`,
		`/*no expectations*/ var a = <A:A></A:A>;`,
	}},
	{js.JSXExpression, []string{
		`var a = <A:A>«{ 22+{a:1}.a }»</A:A>;`,
		`var a = <A:A>bb«{1}»cc«{<abc/>}»</A:A>;`,
		`var a = <a href=«{1+{a:2}.a}»/>;`,
		`/*no expectations*/ var a = <A:A></A:A>;`,
	}},
	{js.JSXSpreadExpression, []string{
		`var a = <A:A>«{...[a,b,c] }»</A:A>;`,
	}},
	{js.JSXLiteral, []string{
		`var a = <a href={"chrome://about"} target=«"123"»/>;`,
	}},

	// Error Recovery
	{js.SyntaxProblem, []string{
		// Parenthesized expressions
		`a = («5+»§)`,
		`a = («a.b[10].»§)`,
		`a = («a,»§) => b;`,

		// Semicolon insertion during recovery.
		`{1} «(1+2) §3»`,
		`function a() { §«*l;» }`, /* not needed */
		`function a() { §«*l» }`,  /* required for recovery */

		// Statements
		`function a() {
		   «var a = 1+» /* inserted semicolon */
		   §«var b = 2+§;»
		   «var c = 2+
		   a §= b;»
		   b = a;
		 }`,

		// Binding
		`function a(i) {
		   let {b: [e,«...»§]} = i;
		   let {c: [«...»§]} = i;
		 }`,
		`function a(i) {
		   let {a: [ §«888»,b,c], c:{q} } = i;
		 }`,
		`function a(i) {
		   let {a: [q, §«888+2»,b,c], c:{p} } = i;
		 }`,
		`function a(i) {
		   let { c:{«8»§}, e:{8:«»§}, d:{8:§«function»} } = i;
		 }`,

		// ObjectLiteral
		`{a=b;} ({«a = b»});`,
		`function a(i) {
		   let a = {«b = 5»};
		   let b = {q: 1, «c: »§};
		 }`,
	}},

	{js.InvalidToken, []string{
		`function a() { «0x» }`,
		`/*fails*/ function a() { «0x»§`,
		`function a() { «"abc»
		}`,
	}},
}

func TestParser(t *testing.T) {
	l := new(js.Lexer)
	p := new(js.Parser)

	seen := map[js.NodeType]bool{}
	for _, tc := range parseTests {
		seen[tc.nt] = true
		for _, input := range tc.inputs {
			test := parsertest.New(t, tc.nt.String(), input)
			l.Init(test.Source())
			errHandler := func(se js.SyntaxError) bool {
				test.ConsumeError(t, se.Offset, se.Endoffset)
				return true
			}
			p.Init(errHandler, func(nt js.NodeType, offset, endoffset int) {
				if nt == tc.nt {
					test.Consume(t, offset, endoffset)
				}
			})
			test.Done(t, p.Parse(l))
		}
	}
	for n := js.NodeType(1); n < js.NodeTypeMax; n++ {
		if !seen[n] {
			t.Errorf("%v is not tested", n)
		}
	}
}

func BenchmarkParser(b *testing.B) {
	l := new(js.Lexer)
	p := new(js.Parser)
	onError := func(se js.SyntaxError) bool {
		b.Errorf("unexpected: %v", se)
		return false
	}

	p.Init(onError, func(t js.NodeType, offset, endoffset int) {})
	for i := 0; i < b.N; i++ {
		l.Init(jsBenchmarkCode)
		p.Parse(l)
	}
	b.SetBytes(int64(len(jsBenchmarkCode)))
}
