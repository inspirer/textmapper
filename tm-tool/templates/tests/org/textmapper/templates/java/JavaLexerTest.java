package org.textmapper.templates.java;

import org.junit.Test;
import org.textmapper.templates.java.JavaLexer.Tokens;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.fail;

/**
 * evgeny, 8/1/12
 */
public class JavaLexerTest {

	@Test
	public void testIdentifiers() {
		assertJava(" String    ", Tokens.Identifier);
		assertJava("i3", Tokens.Identifier);
		assertJava(/* In greek */"\u03b1\u03c1\u03b5\u03c4\u03b7", Tokens.Identifier);
		assertJava("MAX_VALUE", Tokens.Identifier);
		assertJava("isLetterOrDigit", Tokens.Identifier);
		assertJava("is\\u00aa", Tokens.Identifier);
	}

	@Test
	public void testKeywords() {
		assertJava("abstract", Tokens.kw_abstract);
		assertJava("continue", Tokens.kw_continue);
		assertJava("for", Tokens.kw_for);
		assertJava("new", Tokens.kw_new);
		assertJava("switch", Tokens.kw_switch);
		assertJava("assert", Tokens.kw_assert);
		assertJava("default", Tokens.kw_default);
		assertJava("if", Tokens.kw_if);
		assertJava("package", Tokens.kw_package);
		assertJava("synchronized", Tokens.kw_synchronized);
		assertJava("boolean", Tokens.kw_boolean);
		assertJava("do", Tokens.kw_do);
		assertJava("goto", Tokens.kw_goto);
		assertJava("private", Tokens.kw_private);
		assertJava("this", Tokens.kw_this);
		assertJava("break", Tokens.kw_break);
		assertJava("double", Tokens.kw_double);
		assertJava("implements", Tokens.kw_implements);
		assertJava("protected", Tokens.kw_protected);
		assertJava("throw", Tokens.kw_throw);
		assertJava("byte", Tokens.kw_byte);
		assertJava("else", Tokens.kw_else);
		assertJava("import", Tokens.kw_import);
		assertJava("public", Tokens.kw_public);
		assertJava("throws", Tokens.kw_throws);
		assertJava("case", Tokens.kw_case);
		assertJava("enum", Tokens.kw_enum);
		assertJava("instanceof", Tokens.kw_instanceof);
		assertJava("return", Tokens.kw_return);
		assertJava("transient", Tokens.kw_transient);
		assertJava("catch", Tokens.kw_catch);
		assertJava("extends", Tokens.kw_extends);
		assertJava("int", Tokens.kw_int);
		assertJava("short", Tokens.kw_short);
		assertJava("try", Tokens.kw_try);
		assertJava("char", Tokens.kw_char);
		assertJava("final", Tokens.kw_final);
		assertJava("interface", Tokens.kw_interface);
		assertJava("static", Tokens.kw_static);
		assertJava("void", Tokens.kw_void);
		assertJava("class", Tokens.kw_class);
		assertJava("finally", Tokens.kw_finally);
		assertJava("long", Tokens.kw_long);
		assertJava("strictfp", Tokens.kw_strictfp);
		assertJava("volatile", Tokens.kw_volatile);
		assertJava("const", Tokens.kw_const);
		assertJava("float", Tokens.kw_float);
		assertJava("native", Tokens.kw_native);
		assertJava("super", Tokens.kw_super);
		assertJava("while", Tokens.kw_while);
	}

	@Test
	public void testInts() {
		assertJava("0", Tokens.IntegerLiteral);
		assertJava("2", Tokens.IntegerLiteral);
		assertJava("0372", Tokens.IntegerLiteral);
		assertJava("0xDada_Cafe", Tokens.IntegerLiteral);
		assertJava("1996", Tokens.IntegerLiteral);
		assertJava("0x00_FF__00_FF", Tokens.IntegerLiteral);

		assertJava("0l", Tokens.IntegerLiteral);
		assertJava("0777L", Tokens.IntegerLiteral);
		assertJava("0x100000000L", Tokens.IntegerLiteral);
		assertJava("2_147_483_648L", Tokens.IntegerLiteral);
		assertJava("0xC0B0L", Tokens.IntegerLiteral);
	}

	@Test
	public void testFloat() {
		assertJava("1e1f", Tokens.FloatingPointLiteral);
		assertJava("2.f", Tokens.FloatingPointLiteral);
		assertJava(".3f", Tokens.FloatingPointLiteral);
		assertJava("0f", Tokens.FloatingPointLiteral);
		assertJava("3.14f", Tokens.FloatingPointLiteral);
		assertJava("6.022137e+23f", Tokens.FloatingPointLiteral);

		assertJava("1e1", Tokens.FloatingPointLiteral);
		assertJava("2.", Tokens.FloatingPointLiteral);
		assertJava(".3", Tokens.FloatingPointLiteral);
		assertJava("0.0", Tokens.FloatingPointLiteral);
		assertJava("3.14", Tokens.FloatingPointLiteral);
		assertJava("1e-9d", Tokens.FloatingPointLiteral);
		assertJava("1e137", Tokens.FloatingPointLiteral);
	}

	@Test
	public void testChars() {
		assertJava("'a'", Tokens.CharacterLiteral);
		assertJava("'%'", Tokens.CharacterLiteral);
		assertJava("'\\t'", Tokens.CharacterLiteral);
		assertJava("'\\\\'", Tokens.CharacterLiteral);
		assertJava("'\\''", Tokens.CharacterLiteral);
		assertJava("'\\u03a9'", Tokens.CharacterLiteral);
		assertJava("'\\uFFFF'", Tokens.CharacterLiteral);
		assertJava("'\\177'", Tokens.CharacterLiteral);
		assertJava("'\u03a9'", Tokens.CharacterLiteral);
		assertJava("'\u2297'", Tokens.CharacterLiteral);
	}

	@Test
	public void testStrings() {
		assertJava("\"\"", Tokens.StringLiteral);
		assertJava("\"\\\"\"", Tokens.StringLiteral);
		assertJava("\"This is a string\"", Tokens.StringLiteral);
		assertJava("\"\\n\"", Tokens.StringLiteral);
	}

	@Test
	public void testBooleanAndNull() {
		assertJava("null", Tokens.NullLiteral);
		assertJava("true", Tokens.BooleanLiteral);
		assertJava("false", Tokens.BooleanLiteral);
	}

	@Test
	public void testComments() {
		assertComment("x//asdad\n    \ny",
				Tokens.Identifier, Tokens.EndOfLineComment, Tokens.Identifier);

		assertComment("  /**/   ",
				Tokens.TraditionalComment);

		assertComment("x /* this comment /* // /** ends here: */ y",
				Tokens.Identifier, Tokens.TraditionalComment, Tokens.Identifier);
	}

	@Test
	public void testSeparators() {
		assertJava(" (    )    {    }    [    ]    ;    ,    . ",
				Tokens.Lparen, Tokens.Rparen, Tokens.Lcurly, Tokens.Rcurly, Tokens.Lsquare, Tokens.Rsquare,
				Tokens.Semicolon, Tokens.Comma, Tokens.Dot);
	}

	@Test
	public void testOperators() {
		assertJava("=   >   <   !   ~   ?   :",
				Tokens.Equal, Tokens.Greater, Tokens.Less, Tokens.Exclamation, Tokens.Tilde,
				Tokens.Questionmark, Tokens.Colon);

		assertJava("==  <=  >=  !=  &&  ||  ++  --",
				Tokens.EqualEqual, Tokens.LessEqual, Tokens.GreaterEqual, Tokens.ExclamationEqual,
				Tokens.AmpersandAmpersand, Tokens.OrOr, Tokens.PlusPlus, Tokens.MinusMinus);

		assertJava("+   -   *   /   &   |   ^   %   <<   >>   >>>	@",
				Tokens.Plus, Tokens.Minus, Tokens.Mult, Tokens.Slash,
				Tokens.Ampersand, Tokens.Or, Tokens.Xor, Tokens.Percent,
				Tokens.LessLess, Tokens.GreaterGreater, Tokens.GreaterGreaterGreater, Tokens.Atsign);

		assertJava("+=  -=  *=  /=  &=  |=  ^=  %=  <<=  >>=  >>>=",
				Tokens.PlusEqual, Tokens.MinusEqual, Tokens.MultEqual, Tokens.SlashEqual,
				Tokens.AmpersandEqual, Tokens.OrEqual, Tokens.XorEqual, Tokens.PercentEqual,
				Tokens.LessLessEqual, Tokens.GreaterGreaterEqual, Tokens.GreaterGreaterGreaterEqual);
	}

	private void assertJava(String text, int... tokens) {
		try {
			JavaLexer javaLexer = new JavaLexer(new StringReader(text), testReporter());
			JavaLexer.Span next;
			int index = 0;
			while ((next = javaLexer.next()).symbol != Tokens.eoi) {
				if (tokens.length == index) {
					fail("unexpected token after eoi: " + next.symbol + "(" + text.substring(next.offset,
							next.endoffset) + ")");
				}
				if (tokens[index] != next.symbol) {
					fail(next.line + ": got " + next.symbol + "(" + text.substring(next.offset,
							next.endoffset) + ") instead of " + tokens[index]);
				}
				index++;
			}
			if (index < tokens.length) {
				fail("expected token: " + tokens[index]);
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException: " + e.getMessage());
		}
	}

	private void assertComment(String text, int... tokens) {
		try {
			JavaLexer javaLexer = new JavaLexer(new StringReader(text), testReporter()) {
				@Override
				protected boolean createToken(JavaLexer.Span token, int ruleIndex) throws IOException {
					if (token.symbol == Tokens.EndOfLineComment || token.symbol == Tokens.TraditionalComment) {
						return true;
					}
					return super.createToken(token, ruleIndex);
				}
			};
			JavaLexer.Span next;
			int index = 0;
			while ((next = javaLexer.next()).symbol != Tokens.eoi) {
				if (tokens.length == index) {
					fail("unexpected token after eoi: " + next.symbol + "(" + text.substring(next.offset,
							next.endoffset) + ")");
				}
				if (tokens[index] != next.symbol) {
					fail(next.line + ": got " + next.symbol + "(" + text.substring(next.offset,
							next.endoffset) + ") instead of " + tokens[index]);
				}
				index++;
			}
			if (index < tokens.length) {
				fail("expected token: " + tokens[index]);
			}
		} catch (IOException e) {
			e.printStackTrace();
			fail("IOException: " + e.getMessage());
		}
	}

	private JavaLexer.ErrorReporter testReporter() {
		return new JavaLexer.ErrorReporter() {
			@Override
			public void error(String message, int line, int offset, int endoffset) {
				fail(line + ": " + message);
			}
		};
	}
}
