package js_test

import (
	"strings"
	"testing"

	"github.com/inspirer/textmapper/parsers/js"
	"github.com/inspirer/textmapper/parsers/js/token"
	"github.com/inspirer/textmapper/parsers/parsertest"
)

var lexerTests = []struct {
	tok    token.Type
	inputs []string
}{

	{token.IDENTIFIER, []string{
		`«abc» «brea» break`,
		`«abc123»`,
		`«_abc_»`,
	}},
	{token.PRIVATEIDENTIFIER, []string{
		`«#abc» `,
		`«#abc123»`,
		`«#_abc_»`,
	}},
	{token.SINGLELINECOMMENT, []string{
		`«#!/usr/bin/env node»
		 abc`,
		` «// abc»
		  «// abc2»

		  var i = 1; «// here too»

		  «// end-of-file»
		`,
		`«// abc»`,
	}},
	{token.MULTILINECOMMENT, []string{
		`1 / «/* comment */» /aa/.lastIndex`,
		`«/**
		   * comment
		   */»
		 function a() {}`,
	}},
	{token.NUMERICLITERAL, []string{
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
	{token.STRINGLITERAL, []string{
		`«'abc'» + «'Elly\'s'» `,
		`«"abc"» `,
		`«"ab ' and \"  c"» `,
	}},

	// Regular expressions vs division.
	{token.REGULAREXPRESSIONLITERAL, []string{
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
	{token.DIV, []string{
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
	{token.DIVASSIGN, []string{
		`b «/=» --/aaa/.lastIndex`,
	}},

	// Templates.
	{token.NOSUBSTITUTIONTEMPLATE, []string{
		"«`a`»",
		"«`a + \"B\"`»",
		"  «`aa q`»  ",
	}},
	{token.TEMPLATEHEAD, []string{
		"«`${»a}` /",
		"«`aa ${» 'aa' }q${ /aaa/ } `",
		"print«`aa ${» 'aa' }q${ /aaa/ } `",
	}},
	{token.TEMPLATEMIDDLE, []string{
		"`aa ${ 'aa' «}q${» /aaa/ } `",
	}},
	{token.TEMPLATETAIL, []string{
		"`${a«}`» /",
		"`aa ${ 'aa' }q${ /aaa/ «} `»",
		"`Method call: \"${foo({name,text})«}\"`»",
	}},

	// Keywords.
	{token.AWAIT, []string{`«await»`}},
	{token.BREAK, []string{`«break» break2 brea hmm b`}},
	{token.CASE, []string{`«case»`}},
	{token.CATCH, []string{`«catch»`}},
	{token.CLASS, []string{`«class»`}},
	{token.CONST, []string{`«const»`}},
	{token.CONTINUE, []string{`«continue»`}},
	{token.DEBUGGER, []string{`«debugger»`}},
	{token.DEFAULT, []string{`«default»`}},
	{token.DELETE, []string{`«delete»`}},
	{token.DO, []string{`«do»`}},
	{token.ELSE, []string{`«else»`}},
	{token.EXPORT, []string{`«export»`}},
	{token.EXTENDS, []string{`«extends»`}},
	{token.FINALLY, []string{`«finally»`}},
	{token.FOR, []string{`«for»`}},
	{token.FUNCTION, []string{`«function»`}},
	{token.IF, []string{`«if»`}},
	{token.IMPORT, []string{`«import»`}},
	{token.IN, []string{`«in»`}},
	{token.INSTANCEOF, []string{`«instanceof»`}},
	{token.NEW, []string{`«new»`}},
	{token.RETURN, []string{`«return»`}},
	{token.SUPER, []string{`«super»`}},
	{token.SWITCH, []string{`«switch»`}},
	{token.THIS, []string{`«this»`}},
	{token.THROW, []string{`«throw»`}},
	{token.TRY, []string{`«try»`}},
	{token.TYPEOF, []string{`«typeof»`}},
	{token.VAR, []string{`«var»`}},
	{token.VOID, []string{`«void»`}},
	{token.WHILE, []string{`«while»`}},
	{token.WITH, []string{`«with»`}},
	{token.YIELD, []string{`«yield»`}},

	// Reserved keywords.
	{token.ENUM, []string{`«enum»`}},

	// Literals.
	{token.NULL, []string{`«null»`}},
	{token.TRUE, []string{`«true»`}},
	{token.FALSE, []string{`«false»`}},

	// Soft (contextual) keywords.
	{token.ACCESSOR, []string{`«accessor»`}},
	{token.AS, []string{`«as»`}},
	{token.ASSERT, []string{`«assert»`}},
	{token.ASSERTS, []string{`«asserts»`}},
	{token.ASYNC, []string{`«async»`}},
	{token.FROM, []string{`«from»`}},
	{token.GET, []string{`«get»`}},
	{token.LET, []string{`«let»`}},
	{token.OF, []string{`«of»`}},
	{token.SET, []string{`«set»`}},
	{token.STATIC, []string{`«static»`}},
	{token.TARGET, []string{`«target»`}},

	// Typescript keywords.
	{token.ABSTRACT, []string{`«abstract»`}},
	{token.ANY, []string{`«any»`}},
	{token.BIGINT, []string{`«bigint»`}},
	{token.BOOLEAN, []string{`«boolean»`}},
	{token.CONSTRUCTOR, []string{`«constructor»`}},
	{token.DECLARE, []string{`«declare»`}},
	{token.GLOBAL, []string{`«global»`}},
	{token.IMPLEMENTS, []string{`«implements»`}},
	{token.INFER, []string{`«infer»`}},
	{token.INTERFACE, []string{`«interface»`}},
	{token.IS, []string{`«is»`}},
	{token.KEYOF, []string{`«keyof»`}},
	{token.MODULE, []string{`«module»`}},
	{token.NAMESPACE, []string{`«namespace»`}},
	{token.NEVER, []string{`«never»`}},
	{token.NUMBER, []string{`«number»`}},
	{token.OBJECT, []string{`«object»`}},
	{token.OVERRIDE, []string{`«override»`}},
	{token.PRIVATE, []string{`«private»`}},
	{token.PROTECTED, []string{`«protected»`}},
	{token.PUBLIC, []string{`«public»`}},
	{token.READONLY, []string{`«readonly»`}},
	{token.REQUIRE, []string{`«require»`}},
	{token.SATISFIES, []string{`«satisfies»`}},
	{token.STRING, []string{`«string»`}},
	{token.SYMBOL, []string{`«symbol»`}},
	{token.TYPE, []string{`«type»`}},
	{token.UNDEFINED, []string{`«undefined»`}},
	{token.UNIQUE, []string{`«unique»`}},
	{token.UNKNOWN, []string{`«unknown»`}},

	// Operators.
	{token.LBRACE, []string{`«{»`}},
	{token.RBRACE, []string{`«}»`}},
	{token.LPAREN, []string{`«(»`}},
	{token.RPAREN, []string{`«)»`}},
	{token.LBRACK, []string{`«[»`}},
	{token.RBRACK, []string{`«]»`}},
	{token.DOT, []string{`«.»`}},
	{token.DOTDOTDOT, []string{`«...»`}},
	{token.SEMICOLON, []string{`«;»`}},
	{token.COMMA, []string{`«,»`}},
	{token.LT, []string{`«<»`}},
	{token.GT, []string{`«>»`}},
	{token.LTASSIGN, []string{`«<=»`}},
	{token.GTASSIGN, []string{`«>=»`}},
	{token.ASSIGNASSIGN, []string{`«==»`}},
	{token.EXCLASSIGN, []string{`«!=»`}},
	{token.ASSIGNASSIGNASSIGN, []string{`«===»`}},
	{token.EXCLASSIGNASSIGN, []string{`«!==»`}},
	{token.ATSIGN, []string{`«@»`}},
	{token.PLUS, []string{`«+»`}},
	{token.MINUS, []string{`«-»`}},
	{token.MULT, []string{`«*»`}},
	{token.REM, []string{`«%»`}},
	{token.PLUSPLUS, []string{`«++»`}},
	{token.MINUSMINUS, []string{`«--»`}},
	{token.LTLT, []string{`«<<»`}},
	{token.GTGT, []string{`«>>»`}},
	{token.GTGTGT, []string{`«>>>»`}},
	{token.AND, []string{`«&»`}},
	{token.OR, []string{`«|»`}},
	{token.XOR, []string{`«^»`}},
	{token.EXCL, []string{`«!»`}},
	{token.TILDE, []string{`«~»`}},
	{token.ANDAND, []string{`«&&» «&&»&`}},
	{token.OROR, []string{`«||» «||»|`}},
	{token.QUEST, []string{`«?» «?»`, `«?».0`, `«?».9`}},
	{token.QUESTQUEST, []string{`«??» ? ?`}},
	{token.QUESTDOT, []string{`«?.»abc`}},
	{token.COLON, []string{`«:»`}},
	{token.ASSIGN, []string{`«=»`}},
	{token.PLUSASSIGN, []string{`«+=»`}},
	{token.MINUSASSIGN, []string{`«-=»`}},
	{token.MULTASSIGN, []string{`«*=»`}},
	{token.REMASSIGN, []string{`«%=»`}},
	{token.LTLTASSIGN, []string{`«<<=»`}},
	{token.GTGTASSIGN, []string{`«>>=»`}},
	{token.GTGTGTASSIGN, []string{`«>>>=»`}},
	{token.ANDASSIGN, []string{`«&=»`}},
	{token.ORASSIGN, []string{`«|=»`}},
	{token.XORASSIGN, []string{`«^=»`}},
	{token.ASSIGNGT, []string{`«=>»`}},
	{token.MULTMULT, []string{`«**»`}},
	{token.MULTMULTASSIGN, []string{`«**=»`}},
	{token.QUESTQUESTASSIGN, []string{`«??=»`}},
	{token.ORORASSIGN, []string{`«||=» |=`}},
	{token.ANDANDASSIGN, []string{`«&&=»`}},

	{token.JSXSTRINGLITERAL, []string{`
	<A f=«"123"»>{
	   <B ref=«"456"» an={"789"} text=«"4 &quot; 56"»></B>
	}</A>

	`}},
	{token.JSXIDENTIFIER, []string{`	<«A» «f»="123">{
	   <«B» «an»={a+"789"}></«B»>
	}</«A»>`}},
	{token.JSXTEXT, []string{`	<A>« »{
	   <B   >«abc

	   »</B>
	}« »</A>`}},

	{token.INVALID_TOKEN, []string{
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
	seen := map[token.Type]bool{}
	seen[token.WHITESPACE] = true
	seen[token.ERROR] = true
	seen[token.RESOLVESHIFT] = true
	for _, tc := range lexerTests {
		seen[tc.tok] = true
		for _, input := range tc.inputs {
			test := parsertest.New(t, tc.tok.String(), input)
			l.Init(test.Source())
			if strings.Contains(input, "/*ts*/") {
				l.Dialect = js.Typescript
			}
			tok := l.Next()
			for tok != token.EOI {
				if tok == tc.tok {
					s, e := l.Pos()
					test.Consume(t, s, e)
				}
				tok = l.Next()
			}
			test.Done(t, nil)
		}
	}
	for tok := token.EOI + 1; tok < token.NumTokens; tok++ {
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
		for next != token.EOI {
			next = l.Next()
		}
	}
	b.SetBytes(int64(len(jsBenchmarkCode)))
}
