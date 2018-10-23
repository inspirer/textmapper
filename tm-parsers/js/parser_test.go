package js_test

import (
	"context"
	"fmt"
	"testing"

	"github.com/inspirer/textmapper/tm-parsers/js"
	"github.com/inspirer/textmapper/tm-parsers/parsertest"
)

var parseTests = []struct {
	dialect js.Dialect
	nt      js.NodeType
	inputs  []string
}{

	{js.Javascript, js.SingleLineComment, []string{
		` «// abc»
		  «// abc2»
		  var i = 1;     «// 8»
		  «// abc2»`,
	}},
	{js.Javascript, js.MultiLineComment, []string{
		`const a «/* te ** / st */» = 5;`,
		`var a = 5;  «/* abc */»`,
	}},
	{js.Javascript, js.IdentifierReference, []string{
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
	{js.Javascript, js.BindingIdentifier, []string{
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
	{js.Javascript, js.LabelIdentifier, []string{
		`«A»: for (;;) {continue «A»; }`,
		`break «B»;`,
	}},
	{js.Javascript, js.This, []string{
		`const c = «this».e;`,
		"`ab${ «this» }${ «this» }c`",
	}},
	{js.Javascript, js.Regexp, []string{
		`«/abc/i».test('aaa')`,
		`1 / ++«/^[a-z]+\//ii».lastIndex`,
		`var a = «/abc/g», b = («/abc/g»)`,
		`var a = {regexp: «/abc/g»}`,
	}},
	{js.Javascript, js.Parenthesized, []string{
		`c = «(1+2)»;`,
	}},
	{js.Javascript, js.Literal, []string{
		`c = «1» | a[«2»] | «true» | «false» | «1e4»;`,

		// 055 is parsed as an octal literal in 055.5
		`«055»§.5`,
		`«059.5»`,
		`«09.9»`,
		`«011»`,
	}},
	{js.Javascript, js.ArrayLiteral, []string{
		`c = «[4]».concat(«[1, 2, 3]»);`,
	}},
	{js.Javascript, js.SpreadElement, []string{
		`c = [4,,,, «...a»];`,
	}},
	{js.Javascript, js.ObjectLiteral, []string{
		`(«{}»);`,
		`a = «{}»;`,
		`for (let {} of «{}»);`,
		`a = «{a: [], c: 1}»;`,
		`a = «{a: «{}»,}»;`,
		`a = foo(«{name,text}»);`,
	}},
	{js.Javascript, js.ShorthandProperty, []string{
		`{} ({«a»,b: 1 in {}, f() {}, });`,
	}},
	{js.Javascript, js.Property, []string{
		`{} ({a,«b: 1 in {}», f() {}, });`,
		"console.log(`${Object.keys({a,«b: a/a», «c: /abc/g»})}`)",
	}},
	{js.Javascript, js.LiteralPropertyName, []string{
		`{} ({a,«b»: 1 in {}, «f»() {}, get «d»() { return 1;}});`,
		`{} ({* «d»() { yield 1;}});`,
	}},
	{js.Javascript, js.ComputedPropertyName, []string{
		`{} ({* «["a"+i]»() { yield 1;}});`,
	}},
	{js.Javascript, js.Initializer, []string{
		`({a «= b», yield «= {yield}»});`,
		`var a «= 4», b «=[]»;`,
	}},
	{js.Javascript, js.TemplateLiteral, []string{
		"print «`abc`»",
		"«`ab${ expr }${ expr2 }c`»",
		"var a = «`Method call: \"${foo({name,text})}\"`»",
	}},
	{js.Javascript, js.NoSubstitutionTemplate, []string{
		"print «`abc`»",
		"print `ab${«``»}c`", // we also parse nested template literals
	}},
	{js.Javascript, js.TemplateHead, []string{
		"print «`ab${»123}c`",
		"print «`ab${» «`as${»q}p`}c`", // we also parse nested template literals
	}},
	{js.Javascript, js.TemplateMiddle, []string{
		"/*no expectations*/ print `a${123}bc`",
		"print `a${123«}${»234}bc`",
		"print `a${123«}abc${»234}bc`",
		"print `a${12«}abc1${»3«}abc2${»234}bc`",
	}},
	{js.Javascript, js.TemplateTail, []string{
		"print `a${123«}bc`»",
	}},
	{js.Javascript, js.TaggedTemplate, []string{
		"«tpl`ab${ expr }${ expr2 }c`»",
	}},
	{js.Javascript, js.IndexAccess, []string{
		`«super[10]»();`,
		`«super()[10]»();`,
		`«s.a()[10]»();`,
		`««s.a()[10]»[10]»();`,
	}},
	{js.Javascript, js.PropertyAccess, []string{
		`for (««let.a».b» in b);`,
		`«a.b»();`,
		`«a().b»();`,
		`«a()[10].b»();`,
		`«super.me»();`,
	}},
	{js.Javascript, js.SuperExpression, []string{
		`«super».me();`,
	}},
	{js.Javascript, js.NewExpression, []string{
		`«new A»;`,
		`«new A.b[10]»;`,
		`«new A()»;`,
		`«new A.b[10]()».a();`,
		`«new A.b[10]()»[10];`,
	}},
	{js.Javascript, js.NewTarget, []string{
		`«new.target»;`,
	}},
	{js.Javascript, js.CallExpression, []string{
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
	{js.Javascript, js.Arguments, []string{
		`aa«()».bbb«()»«()»;`,
		`aa«(1,2)»;`,
	}},
	{js.Javascript, js.PostInc, []string{
		`a(«b++», c)`,
	}},
	{js.Javascript, js.PostDec, []string{
		`a(«b--», c)`,
	}},
	{js.Javascript, js.PreInc, []string{
		`a(«++b», c)`,
	}},
	{js.Javascript, js.PreDec, []string{
		`a(«--b», c)`,
	}},
	{js.Javascript, js.UnaryExpression, []string{
		`«delete a»
		 «void 1»
		 «typeof s» == "string"
		 a = b
		 ++c;
		 «+1» + «-2» & «~0»
		 if («!(«typeof s» === "string")») {}`,
	}},
	{js.Javascript, js.ExponentiationExpression, []string{
		`/*no expectations*/ 1;`,
		`«1**2»;`,
		`«1**«2**3»»;`,
		`«1**«2**--a»»;`,
		`«--1**«2**a++»»;`,
	}},
	{js.Javascript, js.MultiplicativeExpression, []string{
		`/*no expectations*/ 1;`,
		`«1*2»;`,
		`1+«2/3»;`,
		`«-1%~2» << 3;`,
	}},
	{js.Javascript, js.AdditiveExpression, []string{
		`/*no expectations*/ 1;`,
		`«1+2»;`,
		`a ^ «1+2*3»;`,
		`«1-2» << 3;`,
	}},
	{js.Javascript, js.ShiftExpression, []string{
		`/*no expectations*/ 1;`,
		`«1<<2»;`,
		`«1>>2+3»;`,
		`«1>>>1» < 3;`,
	}},
	{js.Javascript, js.RelationalExpression, []string{
		`/*no expectations*/ 1;`,
		`«1 in {}»;`,
		`«1<2*4»;`,
		`«1>2» != true;`,
		`if («1 >= 1» && «1 <= 1» && «a instanceof Window») alert('true');`,
	}},
	{js.Javascript, js.EqualityExpression, []string{
		`if («a === b»);`,
		`(«c !== 1+1»)`,
		`(«c != 1/2»)`,
		`(«a == 1»)`,
		// Left associative.
		`(««a == 5» === true»)`,
	}},
	{js.Javascript, js.BitwiseANDExpression, []string{
		`(«a&1»)`,
		`(«a==7 & 3+3»)`,
		// Left associative.
		`(««a&7» & 3»)`,
	}},
	{js.Javascript, js.BitwiseXORExpression, []string{
		`(«a^1»)`,
		`(«a&7 ^ 3»)`,
		`(«7 ^ a==3»)`,
		// Left associative.
		`(««a^7» ^ 4»)`,
	}},
	{js.Javascript, js.BitwiseORExpression, []string{
		`(«a|1»)`,
		`(«a&7 | 1»)`,
		`(a & («7|1»))`,
		// Left associative.
		`(««a|7» | 1»)`,
	}},
	{js.Javascript, js.LogicalANDExpression, []string{
		`(«a && true»)`,
		`(«a && a==5»)`,
		// Left associative.
		`(««a && b» && c»)`,
	}},
	{js.Javascript, js.LogicalORExpression, []string{
		`(«a || true»)`,
		// Left associative.
		`(««a || b» || c»)`,
	}},
	{js.Javascript, js.ConditionalExpression, []string{
		`(«a ? b : c»)`,
		`(«a ? «b1 ? b : c» : «b2 ? b : c»»)`,
		`(«false ? «true ? 1 : 2» : 3») == 3;`,
		`«a.b==4 ? (a) : a.b=6»`,
	}},
	{js.Javascript, js.AssignmentExpression, []string{
		`{ «a = 5» }`,
		`{ «a ^= 5» }`,
		`{ «a >>>= 5» }`,
		`{ «a |= 5» }`,
		`{ «a **= 5» }`,
	}},
	{js.Javascript, js.CommaExpression, []string{
		`{ «a = 5, b = 6» }`,
		`{ a = («5,  6») }`,
		`{ a((«5, 6»), 9) }`,
	}},
	{js.Javascript, js.AssignmentOperator, []string{
		` a«+=»1`,
		` a«**=»2`,
		` a«<<=» b «+=» 1`,
	}},
	{js.Javascript, js.Block, []string{
		`«{}»`,
		`«{  «{1+2}»  ({a:b}) }»`,
		`switch(a) «{}»`,
		`switch(a) «{case 1: case 2: default: case 3:}»`,
	}},
	{js.Javascript, js.LexicalDeclaration, []string{
		`«let a;»`,
		`«let a = 5;»`,
		`«let [a] = [5];»`,
		`«const {b: [c]} = a;»`,
	}},
	{js.Javascript, js.LexicalBinding, []string{
		`let «a»;`,
		`let «a = 5»;`,
		`let «[a] = [5]»;`,
		`const «{b: [c]} = a»;`,
	}},
	{js.Javascript, js.VariableStatement, []string{
		`«var a;»`,
		`«var a, b;»`,
		`«var a = 5, b;»`,
		`«var [a, b, c] = x, b;»`,
		`«var q, {names: [name1, name2, ...others],} = param»`,
	}},
	{js.Javascript, js.VariableDeclaration, []string{
		`var «q», «{obj1: {a,b,c}} = param»`,
	}},
	{js.Javascript, js.ObjectPattern, []string{
		`const «{b: [c]}» = a;`,
		`const «{}» = a;`,
		`const «{name}» = a;`,
		`for (const «{b,}» in a);`,
	}},
	{js.Javascript, js.PropertyBinding, []string{
		`const {name, «a: {}»} = a;`,
	}},
	{js.Javascript, js.ElementBinding, []string{
		`let [oth, «[A]», «{a}»] = y;`,
	}},
	{js.Javascript, js.SingleNameBinding, []string{
		`const {«name»} = a;`,
		`let [«name»] = a;`,
		`let [«oth», [«A»], {«a»}] = y;`,
	}},
	{js.Javascript, js.ArrayPattern, []string{
		`var «[]» = x;`,
		`let «[x]» = y;`,
		`let «[x, ...rest]» = y;`,
	}},
	{js.Javascript, js.BindingRestElement, []string{
		`let [x, «...rest»] = y;`,
		`let [«...oth»] = y;`,
	}},
	{js.Javascript, js.EmptyStatement, []string{
		`«;»`,
		`for(;;)«;»`,
		`while(true)«;»`,
		`do«;» while(true);`,
	}},
	{js.Javascript, js.ExpressionStatement, []string{
		`«1+2;» «v();»`,
		`function a() {
			«yield»
			«yield()»
		}`,
	}},
	{js.Javascript, js.IfStatement, []string{
		`«if (a in b); else continue;»`,
		`«if (a in b); else «if (true) a(); else b();»»`,
		`«if (a in b) {«if(a);»} else continue;»`,
	}},
	{js.Javascript, js.DoWhileStatement, []string{
		`«do {} while(a < 5);»`,
		`«do; while(a < 5);»`,
	}},
	{js.Javascript, js.WhileStatement, []string{
		`«while(false in a);»`,
	}},
	{js.Javascript, js.ForStatement, []string{
		`«for (a=0; a < 5; a++);»`,
	}},
	{js.Javascript, js.ForStatementWithVar, []string{
		`«for (var a; a < 5; );»`,
		`«for (var {a,b} = c; a < 5;);»`,
	}},
	{js.Javascript, js.ForCondition, []string{
		`for (a=0; «a < 5»; a++);`,
		`for (var a; «a < 5»; );`,
		`for (var {a,b} = c; «a < 5»;); for (; «»;);`,
	}},
	{js.Javascript, js.ForFinalExpression, []string{
		`for (a=0; a < 5; «a++»);`,
		`for (var a; a < 5; «c++, d++»); for (;; «»);`,
	}},
	{js.Javascript, js.ForInStatement, []string{
		`«for (a in b) continue;»`,
		`«for (let.a in b);»`,
		`«for (let in b);»`,
	}},
	{js.Javascript, js.ForInStatementWithVar, []string{
		`«for (let [a] in b);»`,
		`«for (let a in b);»`,
		`«for (var a in b);»`,
		`«for (var [a] in b);»`,
	}},
	{js.Javascript, js.ForOfStatement, []string{
		`«for (a of b);»`,
	}},
	{js.Javascript, js.ForOfStatementWithVar, []string{
		`«for (var {name:[name]} of b);»`,
		`«for (const [[name]] of b);»`,
	}},
	{js.Javascript, js.ForBinding, []string{
		`for (var «a» in b);`,
		`for (var «[...a]» in b);`,
		`for (const «{}» in b);`,
		`for (let «[a]» in b);`,
		`for (const «[,,,,...a]» in b);`,
	}},
	{js.Javascript, js.ContinueStatement, []string{
		`for(;;){ «continue;» }`,
		`A: do «continue A;» while(false);`,
	}},
	{js.Javascript, js.BreakStatement, []string{
		`for(;;){ «break;» }`,
		`A: do «break A;» while(false);`,
	}},
	{js.Javascript, js.ReturnStatement, []string{
		`function a() { «return 1;» }`,
		`function b() { «return;» }`,
	}},
	{js.Javascript, js.WithStatement, []string{
		`«with(window) aa();» {}`,
		`«with(window) { addListener(); }» {}`,
	}},
	{js.Javascript, js.SwitchStatement, []string{
		`«switch(a) {}»`,
		`«switch(a) {case 1: case 2: default: case 3:}»`,
		`«switch(a) {default: 1; case 3:}»`,
		`«switch(a) {case 3: {} default: 1;}»`,
	}},
	{js.Javascript, js.Case, []string{
		`switch(a) {«case 1: a();»}`,
		`switch(a) {«case 1:» «case 2:» default: «case 3:»}`,
	}},
	{js.Javascript, js.Default, []string{
		`switch(a) {case 1: case 2: «default:» case 3:}`,
		`function a() { switch(a) {«default: return;» case 3:} }`,
	}},
	{js.Javascript, js.LabelledStatement, []string{
		`«yield: do break yield; while(false);»`,
		`«A: for(;;) { break A; }»`,
		`«A: function q() { return; }»`,
	}},
	{js.Javascript, js.ThrowStatement, []string{
		`function a() { «throw new Error('oops!')» }`,
		`function a() { «throw a(1)» }`,
	}},
	{js.Javascript, js.TryStatement, []string{
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
	{js.Javascript, js.Catch, []string{
		`try {} «catch (e) {
		   throw e
		 }»`,
	}},
	{js.Javascript, js.Finally, []string{
		`try {} catch (e) {
		   throw e
		 } «finally {log.console('done')}»`,
		`try {
		   call();
		 } «finally {
		   log.console('done')
		 }»`,
	}},
	{js.Javascript, js.DebuggerStatement, []string{
		`«debugger;»`,
	}},
	{js.Javascript, js.Function, []string{
		`«function id() { yield = 1; }»`,
		`export default «function() { yield = 1; }»`,
		`«function sum(a,b) { return a + b; }»`,
	}},
	{js.Javascript, js.FunctionExpression, []string{
		`(«function() { yield = 1; }»)();`,
		`(«function id() { yield = 1; }»)();`,
		`(«function yield() { a++; }»)();`,
		`(«function as() { a++; }»)();`,
		`(«function let() { a++; }»)();`,
	}},
	{js.Javascript, js.Parameters, []string{
		`(function«(a,b,c)» { yield = 1; })();`,
		`(function«()» { yield = 1; })();`,
		`export default function«()» { yield = 1; };`,
		`function q«(...a)» {}`,

		// in arrow functions
		`a = «(a,)» => b;`,
		`(async «(a)» => a + 1)(1);`,
		`(«(a,b)» => { return a*b; })(1);`,
	}},
	{js.Javascript, js.RestParameter, []string{
		`function q(«...a») {}`,
		`function q(b,c, «...a») {}`,
	}},
	{js.Javascript, js.DefaultParameter, []string{
		`function q(«a», «b») {}`,
		`function q(«[id]», «{name: name}») {}`,
	}},
	{js.Javascript, js.ArrowFunction, []string{
		`(«a => a + 1»)(1);`,
		`(«(a,b) => { return a*b; }»)(1);`,
		`(«(a:A<B<C<D>>>,b) => { return a*b; }»)((a<b>c>>d));`,
		`
		 var a = async
		 «v => 1»

		 var b = async
		 «(v, c) => 1»`,
	}},
	{js.Javascript, js.ConciseBody, []string{
		`(a => «a + 1»)(1);`,
	}},
	{js.Javascript, js.Getter, []string{
		`class A { «get x() { return this.x; }» set x(val) { this.x = val; }}`,
	}},
	{js.Javascript, js.Setter, []string{
		`class A { get x() { return this.x; } «set x(val) { this.x = val; }»}`,
	}},
	{js.Javascript, js.Method, []string{
		`({ private «run(){ console.log('executed'); }»}).run();`,
		`class A { «noop(input) {}»}`,
	}},
	{js.Javascript, js.ObjectMethod, []string{
		`({ «@abc run(){ console.log('executed'); }»}).run();`,
	}},
	{js.Javascript, js.GeneratorMethod, []string{
		`({«*gen(){ yield 1; yield 2; }»}).run();`,
	}},
	{js.Javascript, js.Generator, []string{
		`«function *gen(){ yield 1; yield 2; }» {}`,
		`export default «function*(){ yield 1; yield 2; }» {}`,
	}},
	{js.Javascript, js.GeneratorExpression, []string{
		`(«function*(){ yield 1; yield 2; }»)();`,
		`(«function* a(){ yield 1; yield 2; }»)();`,
	}},
	{js.Javascript, js.Body, []string{
		`(function*()«{ yield 1; yield 2; }»)();`,
		`class A { noop(input) «{}»}`,
		`function a() «{if (false) a();}»`,
		`var a = function() «{if (false) a();}»;`,

		// in arrow functions
		`((a,b) => «{ return a*b; }»)(1);`,
	}},
	{js.Javascript, js.Yield, []string{
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
	{js.Javascript, js.AsyncArrowFunction, []string{
		`for(«async of=>5»;;) {}`,
		`for(«async (of)=>5»;;) {}`,
		`for(«async (of)=>{}»;;) {}`,
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
	{js.Javascript, js.AsyncMethod, []string{
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
	{js.Javascript, js.AsyncFunction, []string{
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
	{js.Javascript, js.AsyncFunctionExpression, []string{
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
	{js.Javascript, js.AwaitExpression, []string{
		`async function gogo() {
       var b = «await func1(10)»;
       return b + 1;
     }`,
		`var f = async v => «await go()»
		 await + 1`,
	}},
	{js.Javascript, js.Class, []string{
		`«class A {}»`,
		`«class A extends B {; ;}»`,
		`«class A extends B { a() {} }»`,
		`export default «class extends B {}»`,
		`export default «class { get a() { return 1;}}»`,
	}},
	{js.Javascript, js.ClassExpr, []string{
		`(«class A {}»);`,
		`(«class A extends B {; ;}»);`,
		`(«class A extends B { a() {} }»);`,
		`(«class extends B {}»);`,
		`(«class { get a() { return 1;}}»);`,
	}},
	{js.Javascript, js.Extends, []string{
		`class A «extends B» {; ;}`,
		`class A «extends compose(B,C)» {}`,
		`(class «extends (A)» {});`,
		`class NonModuleBuilder «extends Foo<Bar>» {}`,
	}},
	{js.Javascript, js.ClassBody, []string{
		`class A extends B «{; ;}»`,
		`class A extends B «{
		   ;
		   a() { return 1}
		 }»`,
	}},
	{js.Javascript, js.EmptyDecl, []string{
		`class A extends B {
		   «;» «;»
		 }`,
	}},
	{js.Javascript, js.MemberMethod, []string{
		`class A extends B {
		   ;
		   «static a() { return 1}»
		   «*a() { yield 1; yield 2}»
		   «static get x() { return this.x}»
		   «set x(val) { this.x = val}»
		 }`,
	}},
	{js.Javascript, js.Static, []string{
		`class A {
		   «static» b() { return 1}
		 }`,
	}},
	{js.Javascript, js.Module, []string{
		`«»`,
		`
		«»`,
		` «a = 4» `,
	}},
	{js.Javascript, js.ImportDeclaration, []string{
		`«import './aaa'»`,
		`«import * as aaa from './aaa'»`,
		`«import {b,c,} from './aaa'»`,
		`«import aaa from './aaa'»`,
		`«import aaa, {t} from './aaa'»`,
		`«import aaa, * as oth from './aaa'»`,
	}},
	{js.Javascript, js.NameSpaceImport, []string{
		`import «* as aaa» from './aaa'`,
		`import aaa, «* as oth» from './aaa'`,
	}},
	{js.Javascript, js.NamedImports, []string{
		`import «{}» from './aaa'`,
		`import aaa, «{t as b}» from './aaa'`,
	}},
	{js.Javascript, js.ImportSpecifier, []string{
		`import {«q»,«o»,} from './aaa'`,
		`import aaa, {«t as b»} from './aaa'`,
	}},
	{js.Javascript, js.ModuleSpecifier, []string{
		`import «'./aaa'»`,
		`import * as aaa from «'./aaa'»`,
	}},
	{js.Javascript, js.ExportDeclaration, []string{
		`«export * from "aa/bb"»`,
		`«export {} from "aa/bb"»`,
		`«export {a, b as c} from "aa/bb"»`,
		`«export {q, t, }»`,
		`«export var v = 5»`,
		`«export const a = 1.2345»`,
		`«export function sum (x, y) { return x + y }»`,
		`«export class A {}»`,
	}},
	{js.Javascript, js.ExportDefault, []string{
		`«export default (x) => x*x»`,
		`«export default class {}»`,
		`«export default function(x, y) { return x + y }»`,
		`«export default function*(x, y) { yield
		                                    yield z
		                                   yield y}»`,
		`«export default 5»`,
		`«export default a = 5»`,
	}},
	{js.Javascript, js.ExportClause, []string{
		`export «{}» from "aa/bb"`,
		`export «{q,}» from "aa/bb"`,
		`export «{q as p, c}» from "aa/bb"`,
	}},
	{js.Javascript, js.ExportSpecifier, []string{
		`export {«a as b», «c», }`,
	}},

	// TODO: https://github.com/tc39/proposal-template-literal-revision (ES '18)

	// JS next: https://tc39.github.io/proposal-object-rest-spread/
	{js.Javascript, js.SpreadProperty, []string{
		`let n = { x, y, «...z» };`,
	}},
	{js.Javascript, js.BindingRestElement, []string{
		`let { x, y, «...z» } = { x: 1, y: 2, a: 3, b: 4 };`,
	}},

	// Automatic Semicolon Insertion
	{js.Javascript, js.InsertedSemicolon, []string{
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
	{js.Javascript, js.JSXElement, []string{
		`var a = «<div>ABC</div>»;`,
		`var a = «<div/>»;`,
		`var a = «<Q>«<P/>»</Q>»;`,
		`var a = «<div>< / div>»;`,
		`var a = «<div / >»;`,
		`var a = «<q>{ «<a href={ {a: {b: 1}}.a.b }/>» }</q>»;`,
		`var a = «<q>{ [1,2,3].map(a => («<a href={a}/>»)) }</q>»;`,
		`var a = «<X comp=«<Y text=«<h1>Title</h1>» />» />»;`,
		"var a = «<input value={`test ${index/4|0}`} disabled={foo%10 ? null : true} />»",
	}},
	{js.Javascript, js.JSXSelfClosingElement, []string{
		`var a = «<div / >»;`,
		`if (alt) return «<div boolean-attr />»;`,
	}},
	{js.Javascript, js.JSXOpeningElement, []string{
		`var a = «<div name="a">»  </div>;`,
	}},
	{js.Javascript, js.JSXClosingElement, []string{
		`var a = <div name="a">  «</div>»;`,
	}},
	{js.Javascript, js.JSXElementName, []string{
		`var a = <«a.b.c.d» />;`,
		`var a = <«a:b» />;`,
		`var a = <«Q» />;`,
	}},
	{js.Javascript, js.JSXNormalAttribute, []string{
		`var a = <div «name="a"»><div «name="b"»></div></div>;`,
	}},
	{js.Javascript, js.JSXSpreadAttribute, []string{
		`var a = <div «{...props}»/>;`,
	}},
	{js.Javascript, js.JSXAttributeName, []string{
		`var a = <div «name»="a"/>;`,
	}},
	{js.Javascript, js.JSXText, []string{
		`var a = <A:A>« ABC »</A:A>;`,
		`var a = <A:A>«bb»{1}«cc»{<abc/>}</A:A>;`,
		`/*no expectations*/ var a = <A:A></A:A>;`,
	}},
	{js.Javascript, js.JSXExpression, []string{
		`var a = <A:A>«{ 22+{a:1}.a }»</A:A>;`,
		`var a = <A:A>bb«{1}»cc«{<abc/>}»</A:A>;`,
		`var a = <a href=«{1+{a:2}.a}»/>;`,
		`/*no expectations*/ var a = <A:A></A:A>;`,
	}},
	{js.Javascript, js.JSXSpreadExpression, []string{
		`var a = <A:A>«{...[a,b,c] }»</A:A>;`,
	}},
	{js.Javascript, js.JSXLiteral, []string{
		`var a = <a href={"chrome://about"} target=«"123"»/>;`,
	}},
	{js.Javascript, js.DecoratorExpr, []string{
		`«@a» «@b.c» class C {};`,
	}},
	{js.Javascript, js.DecoratorCall, []string{
		`«@foo()» «@foo.bar()» class C {};`,
	}},

	// Typescript
	{js.Typescript, js.TsCastExpression, []string{
		`var a = «<string>b»;`,
		`var a = «<string>b.run()»;`,
		`var a = «<T<B>>b.run()»;`,
		`var a = «<T<B<C>>>b.run()»;`,
	}},
	{js.Typescript, js.PredefinedType, []string{
		`let isDone: «boolean» = false;`,
		`let color: «string» = "blue";`,
		`let list: «number»[] = [1, 2, 3];`,
		`let x: [«string», «number»];`,
	}},
	{js.Typescript, js.ArrayType, []string{
		`let list: «number[]» = [1, 2, 3];`,
	}},
	{js.Typescript, js.TupleType, []string{
		`let x: «[string, number]»;`,
	}},
	{js.Typescript, js.ParenthesizedType, []string{
		`var x: «([string, number])»;`,
	}},
	{js.Typescript, js.TypeReference, []string{
		`var x: «abc»;`,
		`function f(foo: «bar») {}`,
	}},
	{js.Typescript, js.ObjectType, []string{
		`var x: «{a: string}»;`,
		`var x: «{a: string;}»;`,
		`var x: «{a: string; [a:string] : never}»;`,
	}},
	{js.Typescript, js.IndexSignature, []string{
		`var x: {«[a:string] : ()=>string»};`,
		`var x: {«[a:string] : <T>(a: T)=>string»};`,
		`var x: {«[a:string] : (a)=>string»};`,
	}},
	{js.Typescript, js.PropertySignature, []string{
		`var x: {«a:string»;};`,
	}},
	{js.Typescript, js.CallSignature, []string{
		`var x: {«(foo) : number»,};`,
		`var x: {«<T>(foo)»,};`,
	}},
	{js.Typescript, js.ConstructSignature, []string{
		`var x: {«new(foo) : number»,};`,
	}},
	{js.Typescript, js.MethodSignature, []string{
		`var x: {«bar(foo) : number»,};`,
		`var x: {«bar<T>(foo)»};`,
	}},
	{js.Typescript, js.ConstructorType, []string{
		`var x: {a: «new() => never»};`,
		`var x: {a: «new(private x : number) => never»};`,
		`var x: {a: «new<Y extends C>(private x : number) => never»};`,
	}},
	{js.Typescript, js.TypeParameters, []string{
		`function identity«<T>»(arg: T): T {}`,
		`var a : «<X, Y extends B>» (abc) => int = 5;`,
		`var x: {[a:string] : «<T>»(a: T)=>string};`,
	}},
	{js.Typescript, js.TypeArguments, []string{
		`function loggingIdentity<T>(arg: Array«<T>»): Array«<T>» {}`,
		`var a = loggingIdentity«<(A«<5>»)>»([1,2,3])`,
		`var a = loggingIdentity«<A«<5>»>»([1,2,3])`,
		`var a = loggingIdentity«<A«<B«<C«<D>»>»>»>»([1,2,3])`,
		`var a = loggingIdentity«<(private abc: number)=> number>» (num)`,
		`var a = loggingIdentity«<()=>()=>(number)>» (num)`,
		`var a = loggingIdentity«< <T>(abc: number) => number>» (num)`,
	}},
	{js.Typescript, js.TypeParameter, []string{
		`function foo<«T», «Q extends T»>() {}`,
		`declare function create<«T extends Foo = Bar»>() : Baz<T>;`,
	}},
	{js.Typescript, js.TypeConstraint, []string{
		`function foo<T, Q «extends T&Foo»>() {}`,
	}},
	{js.Typescript, js.UnionType, []string{
		`function foo<Q extends «T | Foo»>() {}`,
	}},
	{js.Typescript, js.IntersectionType, []string{
		`function foo<Q extends «T & Foo»>() {}`,
	}},
	{js.Typescript, js.ThisType, []string{
		`class Foo {
		   add(operand: number): «this» {}
		 }`,
	}},
	{js.Typescript, js.TypeName, []string{
		`function foo<T>() : «T» {}`,
	}},
	{js.Typescript, js.FunctionType, []string{
		`function foo<T>() : «()=>number» {}`,
		`function foo<T>() : «(abc)=>number» {}`,
		`function foo<T>() : «(abc,def)=>number» {}`,
		`function foo<T>() : «(abc:any)=>number» {}`,
		`function foo<T>() : «(abc?:"abc")=>«()=>any»» {}`,
	}},
	{js.Typescript, js.TypeQuery, []string{
		`function foo(a) : «typeof a.b.c» {}`,
		`function foo(a) : «typeof is» {}`,
	}},
	{js.Typescript, js.TypeAnnotation, []string{
		`var a «: T» = function (kind?«:A») «: B» {}`,
	}},
	{js.Typescript, js.LiteralType, []string{
		`function a(kind?:«"read"») {}`,
		`const TRUE: «true» = true;`,
		`let zeroOrOne: «0» | «1»;`,
		`let plusMinusOne: «-1» | «1»;`,
	}},
	{js.Typescript, js.AccessibilityModifier, []string{
		`function a(«public» kind?:number) {}`,
	}},
	{js.Typescript, js.TypeAliasDeclaration, []string{
		`«type Foo = bar;»`,
		`«type Foo<T> = T»`,
		`«type K1 = keyof Person;»`,
		`«type K3 = keyof { [x: string]: Person };»  // string`,
		`«type Partial<T> = {
       [P in keyof T]?: T[P];
     };»`,
	}},
	{js.Typescript, js.KeyOfType, []string{
		`type K3 = «keyof { [x: string]: Person }»;  // string`,
		`function getProperty<T, K extends «keyof T»>(obj: T, key: K) {}`,
	}},
	{js.Typescript, js.IndexedAccessType, []string{
		`type P1 = «Person["name"]»;  // string
     type P2 = «Person["name" | "age"]»;  // string | number
     type P3 = «string["charAt"]»;  // (pos: number) => string
     type P4 = «string[]["push"]»;  // (...items: string[]) => number
     type P5 = «string[0]»[];  // string`,
	}},
	{js.Typescript, js.MappedType, []string{
		`type Partial<T> = «{ [P in keyof T]?: T[P] }»`,
		`type Readonly<T> = «{ /* */ readonly [P in keyof T]: T[P]; }»`,
	}},
	{js.Typescript, js.TsImplementsClause, []string{
		`class A «implements B» {}
		 class C extends A «implements B» {}`,
	}},
	{js.Typescript, js.MemberVar, []string{
		`class A {
		   «private a = 5;»
                   «private b! : string;»
		   «static a : int = 5;»
		 }`,
	}},
	{js.Typescript, js.Static, []string{
		`class A {
		   «static» a : int = 5;
		   private «static» b() { return 1}
		   «static» displayName?:string;
		 }`,
	}},
	{js.Typescript, js.Abstract, []string{
		`«abstract» class Base {
		   «abstract» name: string;
		   «abstract» get value();
		   «abstract» set value(v: number);
		 }`,
	}},
	{js.Typescript, js.Readonly, []string{
		`class Foo {
		   «readonly» a = 1;
		   «readonly» b: string;
		 }`,
		`export interface ReadonlyMap<T> {
		   forEach(action: (value: T, key: string) => void): void;
		   «readonly» size: number;
		   «readonly» [index: number]: string;
		 }`,
	}},
	{js.Typescript, js.MemberMethod, []string{
		`class A extends B {
		   ;
		   «private static a() { return 1}»
		   «public *a() { yield 1; yield 2}»
		   «protected static get x() { return this.x}»
		   «private set x(val) { this.x = val}»
		 }`,
	}},
	{js.Typescript, js.TsIndexMemberDeclaration, []string{
		`class A {
		   «[a:string] : int;»
		   «[key:number] : string;»
		 }`,
	}},
	{js.Typescript, js.TsInterface, []string{
		`«interface A {}»`,
		`«interface A<Q> extends B { a : int, b : string }»`,
	}},
	{js.Typescript, js.TsInterfaceExtends, []string{
		`interface A<Q> «extends B» { a : int, b : string }`,
	}},
	{js.Typescript, js.TsEnum, []string{
		`«enum A {}»`,
		`«const enum A { X = 1, Y = 2 }»`,
	}},
	{js.Typescript, js.TsEnumBody, []string{
		`const enum A «{ X = 1, Y = 2 }»`,
	}},
	{js.Typescript, js.TsEnumMember, []string{
		`const enum A { «X = 1», «Y = 2», }`,
	}},
	{js.Typescript, js.TsNamespace, []string{
		`«namespace A {}»`,
		`«namespace foo.bar { function a () {} }»`,
		`export «module StaticServices {}»`,
	}},
	{js.Typescript, js.TsNamespaceBody, []string{
		`namespace foo.bar «{ function a () {} }»`,
	}},
	{js.Typescript, js.TsImportAliasDeclaration, []string{
		`«import foo = abc.foo;»`,
	}},
	{js.Typescript, js.TsImportRequireDeclaration, []string{
		`«import foo = require('somefoo');»`,
		`«import foo = require("somefoo");»`,
	}},
	{js.Typescript, js.TsDynamicImport, []string{
		`const foo = «import('foo')»;`,
		`async function getFoo(): Promise<Foo> {
    		const foo = await «import('./foo')»;
    		return foo;
		 }`,
	}},
	{js.Typescript, js.TsExportAssignment, []string{
		`«export = abc;»`,
	}},
	{js.Typescript, js.TsNamespaceExportDeclaration, []string{
		`«export as namespace abc»`,
		`«export as namespace abc;»`,
	}},
	{js.Typescript, js.TsAmbientVar, []string{
		`«declare var a : int, b : string;»`,
		`«declare const i;»`,
		`declare namespace foo { «export const i;» }`,
	}},
	{js.Typescript, js.TsAmbientBinding, []string{
		`declare var «a : int», «b : string»;`,
		`declare const «i»;`,
	}},
	{js.Typescript, js.TsAmbientFunction, []string{
		`«declare function go();»`,
		`«declare function go<T>(a : string) : T;»`,
		`declare namespace foo { «export function go();» }`,
	}},
	{js.Typescript, js.TsAmbientClass, []string{
		`«declare class A {}»`,
		`«declare class A {  [a:number]:string; private static a; private static foo<T>() : string; }»`,
		`declare namespace foo { «export class A {}» }`,
	}},
	{js.Typescript, js.TsAmbientInterface, []string{
		`«declare interface A {}»`,
		`«declare interface A {  [a:number]:string; private static a; private static foo<T>() : string; }»`,
		`declare namespace foo { «export interface A {}» }`,
	}},
	{js.Typescript, js.TsAmbientClassBody, []string{
		`declare class A «{}»`,
		`declare class A «{  [a:number]:string; private static a; private static foo<T>() : string; }»`,
	}},
	{js.Typescript, js.TsAmbientEnum, []string{
		`«declare enum Kind { A, B }»`,
		`declare namespace foo { «export enum A {}» }`,
	}},
	{js.Typescript, js.TsAmbientNamespace, []string{
		`«declare namespace foo.bar {  }»`,
	}},
	{js.Typescript, js.TsAmbientPropertyMember, []string{
		`declare class A {
			«a : Q<X>;»
			«private static b;»
		}`,
	}},
	{js.Typescript, js.TsAmbientFunctionMember, []string{
		`declare class A {
			«a?(x : number) : Q<X>;»
			«private static b(abc);»
		}`,
	}},
	{js.Typescript, js.TsAmbientIndexMember, []string{
		`declare class A {
			«[key : number] : string;»
		}`,
	}},
	{js.Typescript, js.TsAmbientImportAlias, []string{
		`declare namespace foo { «import a = foo;» }`,
		`declare namespace foo { «export import a = foo;» }`,
	}},
	{js.Typescript, js.TsAmbientTypeAlias, []string{
		`declare namespace foo { «type Abc<Foo, Bar> = Function<Foo> | typeof Bar;» }`,
		`«declare type Abc = Foo | Bar;»`,
	}},
	{js.Typescript, js.TsAmbientModule, []string{
		`«declare module "foo" { export = foo; }»`,
		`«declare module foo.bar { export = foo; }»`,
		`declare namespace Foo { «export module bar {}» }`,
	}},
	{js.Typescript, js.TsNonNull, []string{
		`var a = «a!».b
		 var c = «a!»(5)
		 var d = a
		 !(5)`,
		`function foo() { return «foo()!»; }`,
	}},
	{js.Typescript, js.TsAsExpression, []string{
		`var a = «a as b»`,
		`var a = «a as b|c»`,
		`var a = null == «a as b|c&d» && true`,
		`var a = «1 + a as b|c&d» && true`,
		`var a = x(«a as b|c»)
		 let as = 5;
		 var b = c
		 as (T1)  // <- new line`,
		`for (let as = «A as B»; as < 10; as++) {}`,
	}},
	{js.Typescript, js.TypePredicate, []string{
		`function isFish(pet: Fish | Bird): «pet is Fish» {}`,
		`declare class Foo<T> {
      filter<U extends T>(predicate: (x: T) => «x is U»): Foo<U>;
     }`,
	}},
	{js.Typescript, js.TsThisParameter, []string{
		`function f(«this: void») {} /* 2.0 Specifying the type of this for functions */`,
	}},
	{js.Typescript, js.Catch, []string{
		`try {} «catch { throw e }» /* 2.5 Optional catch clause variables */`,
	}},

	// Error Recovery
	{js.Javascript, js.SyntaxProblem, []string{
		// Parenthesized expressions
		`a = («5+»§)`,
		`a = («a.b[10].»§)`,
		`a = («(«function§,») => b»);`,

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
		`var b = (function() {
       «a+§)»
     })();`,
		`var b = (function() {
       «a §b»
     })();`,

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

		// TODO: fix reported ranges.
		`««a = ({§«{{a>>5»}}»})»`,
	}},

	{js.Javascript, js.InvalidToken, []string{
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
	ctx := context.Background()
	for _, tc := range parseTests {
		seen[tc.nt] = true
		for _, input := range tc.inputs {
			test := parsertest.New(t, tc.nt.String(), input)
			l.Init(test.Source())
			l.Dialect = tc.dialect
			errHandler := func(se js.SyntaxError) bool {
				test.ConsumeError(t, se.Offset, se.Endoffset)
				return true
			}
			p.Init(errHandler, func(nt js.NodeType, offset, endoffset int) {
				if nt == tc.nt {
					test.Consume(t, offset, endoffset)
				}
			})
			test.Done(t, p.Parse(ctx, l))
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

	ctx := context.Background()
	p.Init(onError, func(t js.NodeType, offset, endoffset int) {})
	for i := 0; i < b.N; i++ {
		l.Init(jsBenchmarkCode)
		p.Parse(ctx, l)
	}
	b.SetBytes(int64(len(jsBenchmarkCode)))
}

func BenchmarkLookahead(b *testing.B) {
	l := new(js.Lexer)
	p := new(js.Parser)
	onError := func(se js.SyntaxError) bool {
		b.Errorf("unexpected: %v", se)
		return false
	}

	expr := "()=>1"
	for i := 0; i < 10; i++ {
		expr = fmt.Sprintf("(a=(%v)())=>a+1", expr)
	}

	ctx := context.Background()
	p.Init(onError, func(t js.NodeType, offset, endoffset int) {})
	b.ResetTimer()
	for i := 0; i < b.N; i++ {
		l.Init(expr)
		p.Parse(ctx, l)
	}
	b.SetBytes(int64(len(expr)))
}
