/**
 * Copyright 2002-2022 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.textmapper.lapg.regex;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import org.textmapper.lapg.common.CharacterSetImpl;
import org.textmapper.lapg.regex.RegexDefLexer.ErrorReporter;
import org.textmapper.lapg.regex.RegexDefLexer.Span;
import org.textmapper.lapg.regex.RegexDefLexer.Tokens;
import org.textmapper.lapg.regex.RegexDefTree.TextSource;

public class RegexDefParser {

	public static class ParseException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParseException() {
		}
	}

	private final ErrorReporter reporter;

	public RegexDefParser(ErrorReporter reporter) {
		this.reporter = reporter;
	}


	private static final boolean DEBUG_SYNTAX = false;
	TextSource source;
	CharacterSetImpl.Builder setbuilder = new CharacterSetImpl.Builder();
	private static final int[] tmAction = RegexDefLexer.unpack_int(41,
		"\ufffd\uffff\10\0\11\0\31\0\12\0\uffe5\uffff\uffff\uffff\uffff\uffff\14\0\15\0\uffcd" +
		"\uffff\34\0\uffc7\uffff\16\0\uffa5\uffff\1\0\uffff\uffff\17\0\20\0\21\0\22\0\23\0" +
		"\uffff\uffff\uffff\uffff\uff8b\uffff\4\0\5\0\6\0\7\0\35\0\13\0\32\0\uff71\uffff\24" +
		"\0\33\0\2\0\26\0\27\0\30\0\uffff\uffff\ufffe\uffff");

	private static final int[] tmLalr = RegexDefLexer.unpack_int(156,
		"\1\0\uffff\uffff\2\0\uffff\uffff\3\0\uffff\uffff\4\0\uffff\uffff\14\0\uffff\uffff" +
		"\20\0\uffff\uffff\21\0\uffff\uffff\22\0\uffff\uffff\23\0\uffff\uffff\0\0\37\0\15" +
		"\0\37\0\uffff\uffff\ufffe\uffff\1\0\uffff\uffff\2\0\uffff\uffff\3\0\uffff\uffff\4" +
		"\0\uffff\uffff\14\0\uffff\uffff\20\0\uffff\uffff\21\0\uffff\uffff\22\0\uffff\uffff" +
		"\23\0\uffff\uffff\15\0\37\0\16\0\37\0\uffff\uffff\ufffe\uffff\15\0\uffff\uffff\0" +
		"\0\0\0\uffff\uffff\ufffe\uffff\5\0\uffff\uffff\6\0\uffff\uffff\7\0\uffff\uffff\10" +
		"\0\uffff\uffff\0\0\3\0\1\0\3\0\2\0\3\0\3\0\3\0\4\0\3\0\14\0\3\0\15\0\3\0\16\0\3\0" +
		"\20\0\3\0\21\0\3\0\22\0\3\0\23\0\3\0\uffff\uffff\ufffe\uffff\1\0\uffff\uffff\2\0" +
		"\uffff\uffff\3\0\uffff\uffff\4\0\uffff\uffff\14\0\uffff\uffff\20\0\uffff\uffff\21" +
		"\0\uffff\uffff\22\0\uffff\uffff\23\0\uffff\uffff\0\0\36\0\15\0\36\0\16\0\36\0\uffff" +
		"\uffff\ufffe\uffff\1\0\uffff\uffff\2\0\uffff\uffff\3\0\uffff\uffff\4\0\uffff\uffff" +
		"\14\0\uffff\uffff\20\0\uffff\uffff\21\0\uffff\uffff\22\0\uffff\uffff\23\0\uffff\uffff" +
		"\0\0\37\0\15\0\37\0\16\0\37\0\uffff\uffff\ufffe\uffff\1\0\uffff\uffff\2\0\uffff\uffff" +
		"\3\0\uffff\uffff\20\0\uffff\uffff\21\0\uffff\uffff\24\0\25\0\25\0\25\0\uffff\uffff" +
		"\ufffe\uffff");

	private static final int[] tmGoto = RegexDefLexer.unpack_int(32,
		"\0\0\2\0\24\0\46\0\70\0\100\0\102\0\104\0\106\0\110\0\110\0\110\0\110\0\120\0\124" +
		"\0\126\0\126\0\140\0\152\0\162\0\172\0\176\0\206\0\210\0\214\0\224\0\234\0\244\0" +
		"\250\0\262\0\270\0\276\0");

	private static final int[] tmFromTo = RegexDefLexer.unpack_int(190,
		"\47\0\50\0\0\0\1\0\5\0\1\0\6\0\21\0\7\0\21\0\16\0\1\0\26\0\21\0\27\0\21\0\30\0\1" +
		"\0\40\0\44\0\0\0\2\0\5\0\2\0\6\0\22\0\7\0\22\0\16\0\2\0\26\0\22\0\27\0\22\0\30\0" +
		"\2\0\40\0\45\0\0\0\3\0\5\0\3\0\6\0\23\0\7\0\23\0\16\0\3\0\26\0\23\0\27\0\23\0\30" +
		"\0\3\0\40\0\3\0\0\0\4\0\5\0\4\0\16\0\4\0\30\0\4\0\14\0\31\0\14\0\32\0\14\0\33\0\14" +
		"\0\34\0\0\0\5\0\5\0\5\0\16\0\5\0\30\0\5\0\12\0\30\0\20\0\30\0\20\0\36\0\0\0\6\0\5" +
		"\0\6\0\16\0\6\0\30\0\6\0\40\0\6\0\0\0\7\0\5\0\7\0\16\0\7\0\30\0\7\0\40\0\7\0\0\0" +
		"\10\0\5\0\10\0\16\0\10\0\30\0\10\0\0\0\11\0\5\0\11\0\16\0\11\0\30\0\11\0\26\0\37" +
		"\0\27\0\42\0\6\0\24\0\7\0\24\0\26\0\40\0\27\0\40\0\0\0\47\0\0\0\12\0\5\0\20\0\0\0" +
		"\13\0\5\0\13\0\16\0\35\0\30\0\13\0\0\0\14\0\5\0\14\0\16\0\14\0\30\0\14\0\6\0\25\0" +
		"\7\0\25\0\26\0\41\0\27\0\41\0\6\0\26\0\7\0\27\0\0\0\15\0\5\0\15\0\16\0\15\0\30\0" +
		"\15\0\40\0\46\0\0\0\16\0\5\0\16\0\30\0\16\0\0\0\17\0\5\0\17\0\30\0\43\0");

	private static final int[] tmRuleLen = RegexDefLexer.unpack_int(32,
		"\1\0\1\0\3\0\1\0\2\0\2\0\2\0\2\0\1\0\1\0\1\0\3\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0\1\0" +
		"\2\0\2\0\3\0\3\0\3\0\1\0\3\0\3\0\1\0\2\0\1\0\0\0");

	private static final int[] tmRuleSymbol = RegexDefLexer.unpack_int(32,
		"\26\0\27\0\27\0\30\0\30\0\30\0\30\0\30\0\31\0\31\0\31\0\31\0\31\0\31\0\31\0\32\0" +
		"\32\0\32\0\33\0\33\0\33\0\33\0\33\0\33\0\33\0\34\0\34\0\34\0\35\0\35\0\36\0\36\0");

	protected static final String[] tmSymbolNames = new String[] {
		"eoi",
		"char",
		"escaped",
		"charclass",
		"'.'",
		"'*'",
		"'+'",
		"'?'",
		"quantifier",
		"op_minus",
		"op_union",
		"op_intersect",
		"'('",
		"'|'",
		"')'",
		"'(?'",
		"'['",
		"'[^'",
		"expand",
		"kw_eoi",
		"']'",
		"'-'",
		"input",
		"pattern",
		"part",
		"primitive_part",
		"setsymbol",
		"charset",
		"set_primary",
		"parts",
		"partsopt",
	};

	public interface Nonterminals extends Tokens {
		// non-terminals
		int input = 22;
		int pattern = 23;
		int part = 24;
		int primitive_part = 25;
		int setsymbol = 26;
		int charset = 27;
		int set_primary = 28;
		int parts = 29;
		int partsopt = 30;
	}

	/**
	 * -3-n   Lookahead (state id)
	 * -2     Error
	 * -1     Shift
	 * 0..n   Reduce (rule index)
	 */
	protected static int tmAction(int state, int symbol) {
		int p;
		if (tmAction[state] < -2) {
			for (p = -tmAction[state] - 3; tmLalr[p] >= 0; p += 2) {
				if (tmLalr[p] == symbol) {
					break;
				}
			}
			return tmLalr[p + 1];
		}
		return tmAction[state];
	}

	protected static int gotoState(int state, int symbol) {
		int min = tmGoto[symbol], max = tmGoto[symbol + 1];
		int i, e;

		while (min < max) {
			e = (min + max) >> 2 << 1;
			i = tmFromTo[e];
			if (i == state) {
				return tmFromTo[e+1];
			} else if (i < state) {
				min = e + 2;
			} else {
				max = e;
			}
		}
		return -1;
	}

	protected int tmHead;
	protected Span[] tmStack;
	protected Span tmNext;
	protected RegexDefLexer tmLexer;

	public RegexAstPart parse(RegexDefLexer lexer) throws IOException, ParseException {

		tmLexer = lexer;
		tmStack = new Span[1024];
		tmHead = 0;

		tmStack[0] = new Span();
		tmStack[0].state = 0;
		tmNext = tmLexer.next();

		while (tmStack[tmHead].state != 40) {
			int action = tmAction(tmStack[tmHead].state, tmNext.symbol);

			if (action >= 0) {
				reduce(action);
			} else if (action == -1) {
				shift();
			}

			if (action == -2 || tmStack[tmHead].state == -1) {
				break;
			}
		}

		if (tmStack[tmHead].state != 40) {
			reporter.error(MessageFormat.format("syntax error before line {0}",
								tmLexer.getTokenLine()), tmNext.offset, tmNext.endoffset);
			throw new ParseException();
		}
		return (RegexAstPart)tmStack[tmHead - 1].value;
	}

	protected void shift() throws IOException {
		tmStack[++tmHead] = tmNext;
		tmStack[tmHead].state = gotoState(tmStack[tmHead - 1].state, tmNext.symbol);
		if (DEBUG_SYNTAX) {
			System.out.println(MessageFormat.format("shift: {0} ({1})", tmSymbolNames[tmNext.symbol], tmLexer.tokenText()));
		}
		if (tmStack[tmHead].state != -1 && tmNext.symbol != 0) {
			tmNext = tmLexer.next();
		}
	}

	protected void reduce(int rule) {
		Span left = new Span();
		left.value = (tmRuleLen[rule] != 0) ? tmStack[tmHead + 1 - tmRuleLen[rule]].value : null;
		left.symbol = tmRuleSymbol[rule];
		left.state = 0;
		if (DEBUG_SYNTAX) {
			System.out.println("reduce to " + tmSymbolNames[tmRuleSymbol[rule]]);
		}
		Span startsym = (tmRuleLen[rule] != 0) ? tmStack[tmHead + 1 - tmRuleLen[rule]] : tmNext;
		left.offset = startsym.offset;
		left.endoffset = (tmRuleLen[rule] != 0) ? tmStack[tmHead].endoffset : tmNext.offset;
		applyRule(left, rule, tmRuleLen[rule]);
		for (int e = tmRuleLen[rule]; e > 0; e--) {
			tmStack[tmHead--] = null;
		}
		tmStack[++tmHead] = left;
		tmStack[tmHead].state = gotoState(tmStack[tmHead - 1].state, left.symbol);
	}

	@SuppressWarnings("unchecked")
	protected void applyRule(Span tmLeft, int ruleIndex, int ruleLength) {
		switch (ruleIndex) {
			case 1:  // pattern : partsopt
				{ tmLeft.value = RegexUtil.emptyIfNull(((RegexAstPart)tmStack[tmHead].value), source, tmStack[tmHead].offset); }
				break;
			case 2:  // pattern : pattern '|' partsopt
				{ tmLeft.value = RegexUtil.createOr(((RegexAstPart)tmStack[tmHead - 2].value), ((RegexAstPart)tmStack[tmHead].value), source, tmStack[tmHead].offset); }
				break;
			case 4:  // part : primitive_part '*'
				{ tmLeft.value = new RegexAstQuantifier(((RegexAstPart)tmStack[tmHead - 1].value), 0, -1, source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 5:  // part : primitive_part '+'
				{ tmLeft.value = new RegexAstQuantifier(((RegexAstPart)tmStack[tmHead - 1].value), 1, -1, source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 6:  // part : primitive_part '?'
				{ tmLeft.value = new RegexAstQuantifier(((RegexAstPart)tmStack[tmHead - 1].value), 0, 1, source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 7:  // part : primitive_part quantifier
				{ tmLeft.value = RegexUtil.createQuantifier(((RegexAstPart)tmStack[tmHead - 1].value), source, tmStack[tmHead].offset, tmLeft.endoffset, reporter); }
				break;
			case 8:  // primitive_part : char
				{ tmLeft.value = new RegexAstChar(((Integer)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 9:  // primitive_part : escaped
				{ tmLeft.value = new RegexAstChar(((Integer)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 10:  // primitive_part : '.'
				{ tmLeft.value = new RegexAstAny(source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 11:  // primitive_part : '(' pattern ')'
				{ tmLeft.value = RegexUtil.wrap(((RegexAstPart)tmStack[tmHead - 1].value), tmLeft.offset, tmLeft.endoffset); }
				break;
			case 12:  // primitive_part : expand
				{ tmLeft.value = new RegexAstExpand(source, tmLeft.offset, tmLeft.endoffset); RegexUtil.checkExpand((RegexAstExpand) tmLeft.value, reporter); }
				break;
			case 13:  // primitive_part : kw_eoi
				{ tmLeft.value = new RegexAstChar(-1, source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 15:  // setsymbol : char
				{ tmLeft.value = new RegexAstChar(((Integer)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 16:  // setsymbol : escaped
				{ tmLeft.value = new RegexAstChar(((Integer)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 17:  // setsymbol : charclass
				{ tmLeft.value = new RegexAstCharClass(((String)tmStack[tmHead].value), RegexUtil.getClassSet(((String)tmStack[tmHead].value), setbuilder, reporter, tmLeft.offset, tmLeft.endoffset), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 18:  // charset : '-'
				{ tmLeft.value = new ArrayList<RegexAstPart>(); ((List<RegexAstPart>)tmLeft.value).add(new RegexAstChar('-', source, tmStack[tmHead].offset, tmStack[tmHead].endoffset)); }
				break;
			case 19:  // charset : setsymbol
				{ tmLeft.value = new ArrayList<RegexAstPart>(); RegexUtil.addSetSymbol(((List<RegexAstPart>)tmLeft.value), ((RegexAstPart)tmStack[tmHead].value), reporter); }
				break;
			case 20:  // charset : charset setsymbol
				{ RegexUtil.addSetSymbol(((List<RegexAstPart>)tmStack[tmHead - 1].value), ((RegexAstPart)tmStack[tmHead].value), reporter); }
				break;
			case 21:  // charset : charset '-' %prec char
				{ ((List<RegexAstPart>)tmStack[tmHead - 1].value).add(new RegexAstChar('-', source, tmStack[tmHead].offset, tmStack[tmHead].endoffset)); }
				break;
			case 22:  // charset : charset '-' char
				{ RegexUtil.applyRange(((List<RegexAstPart>)tmStack[tmHead - 2].value), new RegexAstChar(((Integer)tmStack[tmHead].value), source, tmStack[tmHead].offset, tmStack[tmHead].endoffset), reporter); }
				break;
			case 23:  // charset : charset '-' escaped
				{ RegexUtil.applyRange(((List<RegexAstPart>)tmStack[tmHead - 2].value), new RegexAstChar(((Integer)tmStack[tmHead].value), source, tmStack[tmHead].offset, tmStack[tmHead].endoffset), reporter); }
				break;
			case 24:  // charset : charset '-' set_primary
				{ RegexUtil.subtract(((List<RegexAstPart>)tmStack[tmHead - 2].value), ((RegexAstSet)tmStack[tmHead].value), reporter); }
				break;
			case 25:  // set_primary : charclass
				{ tmLeft.value = new RegexAstCharClass(((String)tmStack[tmHead].value), RegexUtil.getClassSet(((String)tmStack[tmHead].value), setbuilder, reporter, tmLeft.offset, tmLeft.endoffset), source, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 26:  // set_primary : '[' charset ']'
				{ tmLeft.value = RegexUtil.toSet(((List<RegexAstPart>)tmStack[tmHead - 1].value), reporter, setbuilder, false, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 27:  // set_primary : '[^' charset ']'
				{ tmLeft.value = RegexUtil.toSet(((List<RegexAstPart>)tmStack[tmHead - 1].value), reporter, setbuilder, true, tmLeft.offset, tmLeft.endoffset); }
				break;
			case 29:  // parts : parts part
				{ tmLeft.value = RegexUtil.createSequence(((RegexAstPart)tmStack[tmHead - 1].value), ((RegexAstPart)tmStack[tmHead].value)); }
				break;
		}
	}
}
