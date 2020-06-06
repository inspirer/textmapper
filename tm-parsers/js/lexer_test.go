package js_test

import (
	"strings"
	"testing"

	"github.com/inspirer/textmapper/tm-parsers/js"
	"github.com/inspirer/textmapper/tm-parsers/parsertest"
)

var lexerTests = []struct {
	tok    js.Token
	inputs []string
}{

	{js.IDENTIFIER, []string{
		`«abc» «brea» break`,
		`«abc123»`,
		`«_abc_»`,
	}},
	{js.PRIVATEIDENTIFIER, []string{
		`«#abc» `,
		`«#abc123»`,
		`«#_abc_»`,
	}},
	{js.SINGLELINECOMMENT, []string{
		`«#!/usr/bin/env node»
		 abc`,
		` «// abc»
		  «// abc2»

		  var i = 1; «// here too»

		  «// end-of-file»
		`,
		`«// abc»`,
	}},
	{js.MULTILINECOMMENT, []string{
		`1 / «/* comment */» /aa/.lastIndex`,
		`«/**
		   * comment
		   */»
		 function a() {}`,
	}},
	{js.NUMERICLITERAL, []string{
		`«1e+9» «1.1e-9» «0xabcdefabcedef123» «123123121»  «0» -«1» `,
		`«0000»`,
		`«0055»`,
		`«0059.5»`,
		`«095.5»`,
		`«0xa_b_c»`,
		`«0B0000_1111»`,
		`«0o777_777»`,
		`«1_1_2.1_1»`,
		`«.1__1»`,
		`«00001__1»`,
		`«000e+01__1»`,
		`«008_9»`,
	}},
	{js.STRINGLITERAL, []string{
		`«'abc'» + «'Elly\'s'» `,
		`«"abc"» `,
		`«"ab ' and \"  c"» `,
	}},

	// Regular expressions vs division.
	{js.REGULAREXPRESSIONLITERAL, []string{
		`var c = «/abc/» // comment
		`,
		`1 / /* comment */ «/aa/».lastIndex`,
		`«/aa/».lastIndex /* comment */ / 1`,
		`++«/aa/».length`,
		`--«/aa/».length`,
		`(--
	  «/aa/».lastIndex)`,
		`b
	  --«/aaa/».lastIndex`,
		`b /= --«/aaa/».lastIndex`,
		`typeof «/aaa/»`,
		`(a) == «/aaa/».lastIndex`,
		`(a) / «/aaa/».lastIndex`,
		`[a] / «/aaa/».lastIndex`,
		"`aa ${ «/aaa/».lastIndex / 1 }q`",
		"`aa ${ 'aa' }q${ «/aaa/» } `",
		`const a = !«/foo/»;   /*ts*/`,
		`do !«/foo/».test(f); while(true);`,

		// TODO if (a) /aaa/.compile()
	}},
	{js.DIV, []string{
		`1 «/» /* comment */ /aa/.lastIndex`,
		`1«/»2;`,
		`var c = 1«/»2;`,
		`b--
	    «/» 3`,
		"`a` «/»",
		`let «/»`,
		"`aa ${ /aaa/.lastIndex «/» 1 }q`",
		`const a = 1 «/» a.b! «/» 2; /*ts*/`,
	}},
	{js.DIVASSIGN, []string{
		`b «/=» --/aaa/.lastIndex`,
	}},

	// Templates.
	{js.NOSUBSTITUTIONTEMPLATE, []string{
		"«`a`»",
		"«`a + \"B\"`»",
		"  «`aa q`»  ",
	}},
	{js.TEMPLATEHEAD, []string{
		"«`${»a}` /",
		"«`aa ${» 'aa' }q${ /aaa/ } `",
		"print«`aa ${» 'aa' }q${ /aaa/ } `",
	}},
	{js.TEMPLATEMIDDLE, []string{
		"`aa ${ 'aa' «}q${» /aaa/ } `",
	}},
	{js.TEMPLATETAIL, []string{
		"`${a«}`» /",
		"`aa ${ 'aa' }q${ /aaa/ «} `»",
		"`Method call: \"${foo({name,text})«}\"`»",
	}},

	// Keywords.
	{js.AWAIT, []string{`«await»`}},
	{js.BREAK, []string{`«break» break2 brea hmm b`}},
	{js.CASE, []string{`«case»`}},
	{js.CATCH, []string{`«catch»`}},
	{js.CLASS, []string{`«class»`}},
	{js.CONST, []string{`«const»`}},
	{js.CONTINUE, []string{`«continue»`}},
	{js.DEBUGGER, []string{`«debugger»`}},
	{js.DEFAULT, []string{`«default»`}},
	{js.DELETE, []string{`«delete»`}},
	{js.DO, []string{`«do»`}},
	{js.ELSE, []string{`«else»`}},
	{js.EXPORT, []string{`«export»`}},
	{js.EXTENDS, []string{`«extends»`}},
	{js.FINALLY, []string{`«finally»`}},
	{js.FOR, []string{`«for»`}},
	{js.FUNCTION, []string{`«function»`}},
	{js.IF, []string{`«if»`}},
	{js.IMPORT, []string{`«import»`}},
	{js.IN, []string{`«in»`}},
	{js.INSTANCEOF, []string{`«instanceof»`}},
	{js.NEW, []string{`«new»`}},
	{js.RETURN, []string{`«return»`}},
	{js.SUPER, []string{`«super»`}},
	{js.SWITCH, []string{`«switch»`}},
	{js.THIS, []string{`«this»`}},
	{js.THROW, []string{`«throw»`}},
	{js.TRY, []string{`«try»`}},
	{js.TYPEOF, []string{`«typeof»`}},
	{js.VAR, []string{`«var»`}},
	{js.VOID, []string{`«void»`}},
	{js.WHILE, []string{`«while»`}},
	{js.WITH, []string{`«with»`}},
	{js.YIELD, []string{`«yield»`}},

	// Reserved keywords.
	{js.ENUM, []string{`«enum»`}},

	// Literals.
	{js.NULL, []string{`«null»`}},
	{js.TRUE, []string{`«true»`}},
	{js.FALSE, []string{`«false»`}},

	// Soft (contextual) keywords.
	{js.AS, []string{`«as»`}},
	{js.ASSERTS, []string{`«asserts»`}},
	{js.ASYNC, []string{`«async»`}},
	{js.FROM, []string{`«from»`}},
	{js.GET, []string{`«get»`}},
	{js.LET, []string{`«let»`}},
	{js.OF, []string{`«of»`}},
	{js.SET, []string{`«set»`}},
	{js.STATIC, []string{`«static»`}},
	{js.TARGET, []string{`«target»`}},

	// Typescript keywords.
	{js.IMPLEMENTS, []string{`«implements»`}},
	{js.INTERFACE, []string{`«interface»`}},
	{js.PRIVATE, []string{`«private»`}},
	{js.PROTECTED, []string{`«protected»`}},
	{js.PUBLIC, []string{`«public»`}},
	{js.ANY, []string{`«any»`}},
	{js.UNKNOWN, []string{`«unknown»`}},
	{js.BOOLEAN, []string{`«boolean»`}},
	{js.NUMBER, []string{`«number»`}},
	{js.STRING, []string{`«string»`}},
	{js.SYMBOL, []string{`«symbol»`}},
	{js.ABSTRACT, []string{`«abstract»`}},
	{js.CONSTRUCTOR, []string{`«constructor»`}},
	{js.DECLARE, []string{`«declare»`}},
	{js.IS, []string{`«is»`}},
	{js.MODULE, []string{`«module»`}},
	{js.GLOBAL, []string{`«global»`}},
	{js.NAMESPACE, []string{`«namespace»`}},
	{js.REQUIRE, []string{`«require»`}},
	{js.TYPE, []string{`«type»`}},
	{js.READONLY, []string{`«readonly»`}},
	{js.KEYOF, []string{`«keyof»`}},
	{js.UNIQUE, []string{`«unique»`}},
	{js.INFER, []string{`«infer»`}},

	// Operators.
	{js.LBRACE, []string{`«{»`}},
	{js.RBRACE, []string{`«}»`}},
	{js.LPAREN, []string{`«(»`}},
	{js.RPAREN, []string{`«)»`}},
	{js.LBRACK, []string{`«[»`}},
	{js.RBRACK, []string{`«]»`}},
	{js.DOT, []string{`«.»`}},
	{js.DOTDOTDOT, []string{`«...»`}},
	{js.SEMICOLON, []string{`«;»`}},
	{js.COMMA, []string{`«,»`}},
	{js.LT, []string{`«<»`}},
	{js.GT, []string{`«>»`}},
	{js.LTASSIGN, []string{`«<=»`}},
	{js.GTASSIGN, []string{`«>=»`}},
	{js.ASSIGNASSIGN, []string{`«==»`}},
	{js.EXCLASSIGN, []string{`«!=»`}},
	{js.ASSIGNASSIGNASSIGN, []string{`«===»`}},
	{js.EXCLASSIGNASSIGN, []string{`«!==»`}},
	{js.ATSIGN, []string{`«@»`}},
	{js.PLUS, []string{`«+»`}},
	{js.MINUS, []string{`«-»`}},
	{js.MULT, []string{`«*»`}},
	{js.REM, []string{`«%»`}},
	{js.PLUSPLUS, []string{`«++»`}},
	{js.MINUSMINUS, []string{`«--»`}},
	{js.LTLT, []string{`«<<»`}},
	{js.GTGT, []string{`«>>»`}},
	{js.GTGTGT, []string{`«>>>»`}},
	{js.AND, []string{`«&»`}},
	{js.OR, []string{`«|»`}},
	{js.XOR, []string{`«^»`}},
	{js.EXCL, []string{`«!»`}},
	{js.TILDE, []string{`«~»`}},
	{js.ANDAND, []string{`«&&» «&&»&`}},
	{js.OROR, []string{`«||» «||»|`}},
	{js.QUEST, []string{`«?» «?»`, `«?».0`, `«?».9`}},
	{js.QUESTQUEST, []string{`«??» ? ?`}},
	{js.QUESTDOT, []string{`«?.»abc`}},
	{js.COLON, []string{`«:»`}},
	{js.ASSIGN, []string{`«=»`}},
	{js.PLUSASSIGN, []string{`«+=»`}},
	{js.MINUSASSIGN, []string{`«-=»`}},
	{js.MULTASSIGN, []string{`«*=»`}},
	{js.REMASSIGN, []string{`«%=»`}},
	{js.LTLTASSIGN, []string{`«<<=»`}},
	{js.GTGTASSIGN, []string{`«>>=»`}},
	{js.GTGTGTASSIGN, []string{`«>>>=»`}},
	{js.ANDASSIGN, []string{`«&=»`}},
	{js.ORASSIGN, []string{`«|=»`}},
	{js.XORASSIGN, []string{`«^=»`}},
	{js.ASSIGNGT, []string{`«=>»`}},
	{js.MULTMULT, []string{`«**»`}},
	{js.MULTMULTASSIGN, []string{`«**=»`}},

	{js.JSXSTRINGLITERAL, []string{`
	<A f=«"123"»>{
	   <B ref=«"456"» an={"789"} text=«"4 &quot; 56"»></B>
	}</A>

	`}},
	{js.JSXIDENTIFIER, []string{`	<«A» «f»="123">{
	   <«B» «an»={a+"789"}></«B»>
	}</«A»>`}},
	{js.JSXTEXT, []string{`	<A>« »{
	   <B   >«abc

	   »</B>
	}« »</A>`}},

	{js.INVALID_TOKEN, []string{
		` «..» `,
		` «0x»`,
		` «0X» `,
		` «0o» «0b» «0O» «0B»`,
		` «ab\» «ab\u1»`,
		` «#ab\» `,
		` «\u{» `,
		` «\u{0a» `,
		` «0_89»`,
		` «1__»`,
		` «089_»`,
		` «1_.»`,
		` «1_.1_e1_»`,
		` «1_.89»`,
		` «1.89__»`,
		` «0xa_b___»`,
		` «0o1_2___»`,
		` «0o1_2___d»`,
		` «0b1_2_»`,
	}},
}

func TestLexer(t *testing.T) {
	l := new(js.Lexer)
	seen := map[js.Token]bool{}
	seen[js.WHITESPACE] = true
	seen[js.ERROR] = true
	seen[js.RESOLVESHIFT] = true
	for _, tc := range lexerTests {
		seen[tc.tok] = true
		for _, input := range tc.inputs {
			test := parsertest.New(t, tc.tok.String(), input)
			l.Init(test.Source())
			if strings.Contains(input, "/*ts*/") {
				l.Dialect = js.Typescript
			}
			tok := l.Next()
			for tok != js.EOI {
				if tok == tc.tok {
					s, e := l.Pos()
					test.Consume(t, s, e)
				}
				tok = l.Next()
			}
			test.Done(t, nil)
		}
	}
	for tok := js.EOI + 1; tok < js.NumTokens; tok++ {
		if !seen[tok] {
			t.Errorf("%v is not tested", tok)
		}
	}
}

const jsBenchmarkCode = `
  const a = 15;
  if (" abcd ".length == 20) { /aaaa/.test('vvaaaaaaaa') }
  var e = "some very long string" + [123,  9000000].length;
  for (; b < a; b++) { }
  var c = (function() {})();

  ({ reload:  function*() { yield 1; }, b : "aaaaaaaa"}).a();

  class A extends B {
      constructor() { this.x = 1; }
      func() { return this.x; }
  }
  /* lorem ipsum */
`

func BenchmarkLexer(b *testing.B) {
	l := new(js.Lexer)
	for i := 0; i < b.N; i++ {
		l.Init(jsBenchmarkCode)
		next := l.Next()
		for next != js.EOI {
			next = l.Next()
		}
	}
	b.SetBytes(int64(len(jsBenchmarkCode)))
}
