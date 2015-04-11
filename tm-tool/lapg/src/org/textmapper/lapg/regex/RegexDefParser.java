/**
 * Copyright 2002-2015 Evgeny Gryaznov
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
	private static final int[] tmAction = RegexDefLexer.unpack_int(39,
		"\ufffd\uffff\11\0\12\0\13\0\14\0\uffe5\uffff\uffff\uffff\uffff\uffff\20\0\1\0\uffcf" +
		"\uffff\32\0\uffc9\uffff\uffa9\uffff\2\0\uffff\uffff\21\0\22\0\23\0\24\0\25\0\uffff" +
		"\uffff\uffff\uffff\uff91\uffff\5\0\6\0\7\0\10\0\33\0\15\0\16\0\uff79\uffff\26\0\17" +
		"\0\3\0\30\0\31\0\uffff\uffff\ufffe\uffff");

	private static final short[] tmLalr = RegexDefLexer.unpack_short(144,
		"\1\uffff\2\uffff\3\uffff\4\uffff\14\uffff\20\uffff\21\uffff\22\uffff\23\uffff\0\35" +
		"\15\35\uffff\ufffe\1\uffff\2\uffff\3\uffff\4\uffff\14\uffff\20\uffff\21\uffff\22" +
		"\uffff\15\35\16\35\uffff\ufffe\15\uffff\0\0\uffff\ufffe\5\uffff\6\uffff\7\uffff\10" +
		"\uffff\0\4\1\4\2\4\3\4\4\4\14\4\15\4\16\4\20\4\21\4\22\4\uffff\ufffe\1\uffff\2\uffff" +
		"\3\uffff\4\uffff\14\uffff\20\uffff\21\uffff\22\uffff\0\34\15\34\16\34\uffff\ufffe" +
		"\1\uffff\2\uffff\3\uffff\4\uffff\14\uffff\20\uffff\21\uffff\22\uffff\0\35\15\35\16" +
		"\35\uffff\ufffe\1\uffff\2\uffff\3\27\24\27\25\27\uffff\ufffe");

	private static final short[] lapg_sym_goto = RegexDefLexer.unpack_short(31,
		"\0\1\12\23\33\37\40\41\42\43\43\43\43\47\51\52\52\56\62\66\67\71\75\76\100\104\110" +
		"\114\116\121\124");

	private static final short[] lapg_sym_from = RegexDefLexer.unpack_short(84,
		"\45\0\5\6\7\15\25\26\27\37\0\5\6\7\15\25\26\27\37\0\5\6\7\15\25\26\27\0\5\15\27\14" +
		"\14\14\14\0\5\15\27\12\17\17\0\5\15\27\0\5\15\27\0\5\15\27\0\25\26\6\7\25\26\0\0" +
		"\5\0\5\15\27\0\5\15\27\6\7\25\26\6\7\0\5\27\0\5\27");

	private static final short[] lapg_sym_to = RegexDefLexer.unpack_short(84,
		"\46\1\1\20\20\1\20\20\1\43\2\2\21\21\2\21\21\2\44\3\3\22\22\3\22\22\3\4\4\4\4\30" +
		"\31\32\33\5\5\5\5\27\27\35\6\6\6\6\7\7\7\7\10\10\10\10\11\36\41\23\23\37\37\45\12" +
		"\17\13\13\34\13\14\14\14\14\24\24\40\40\25\26\15\15\15\16\16\42");

	private static final short[] tmRuleLen = RegexDefLexer.unpack_short(30,
		"\1\1\1\3\1\2\2\2\2\1\1\1\1\3\3\3\1\1\1\1\1\1\2\2\3\3\1\2\1\0");

	private static final short[] tmRuleSymbol = RegexDefLexer.unpack_short(30,
		"\26\26\27\27\30\30\30\30\30\31\31\31\31\31\31\31\31\32\32\32\33\33\33\33\33\33\34" +
		"\34\35\35");

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
		"parts",
		"partsopt",
	};

	public interface Nonterminals extends Tokens {
		// non-terminals
		static final int input = 22;
		static final int pattern = 23;
		static final int part = 24;
		static final int primitive_part = 25;
		static final int setsymbol = 26;
		static final int charset = 27;
		static final int parts = 28;
		static final int partsopt = 29;
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

	protected static int tmGoto(int state, int symbol) {
		int min = lapg_sym_goto[symbol], max = lapg_sym_goto[symbol + 1] - 1;
		int i, e;

		while (min <= max) {
			e = (min + max) >> 1;
			i = lapg_sym_from[e];
			if (i == state) {
				return lapg_sym_to[e];
			} else if (i < state) {
				min = e + 1;
			} else {
				max = e - 1;
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

		while (tmStack[tmHead].state != 38) {
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

		if (tmStack[tmHead].state != 38) {
			reporter.error(MessageFormat.format("syntax error before line {0}",
								tmLexer.getTokenLine()), tmNext.offset, tmNext.endoffset);
			throw new ParseException();
		}
		return (RegexAstPart)tmStack[tmHead - 1].value;
	}

	protected void shift() throws IOException {
		tmStack[++tmHead] = tmNext;
		tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, tmNext.symbol);
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
		tmStack[tmHead].state = tmGoto(tmStack[tmHead - 1].state, left.symbol);
	}

	@SuppressWarnings("unchecked")
	protected void applyRule(Span tmLeft, int ruleIndex, int ruleLength) {
		switch (ruleIndex) {
			case 1:  // input ::= kw_eoi
				 tmLeft.value = new RegexAstChar(-1, source, tmLeft.offset, tmLeft.endoffset); 
				break;
			case 2:  // pattern ::= partsopt
				 tmLeft.value = RegexUtil.emptyIfNull(((RegexAstPart)tmStack[tmHead].value), source, tmStack[tmHead].offset); 
				break;
			case 3:  // pattern ::= pattern '|' partsopt
				 tmLeft.value = RegexUtil.createOr(((RegexAstPart)tmStack[tmHead - 2].value), ((RegexAstPart)tmStack[tmHead].value), source, tmStack[tmHead].offset); 
				break;
			case 5:  // part ::= primitive_part '*'
				 tmLeft.value = new RegexAstQuantifier(((RegexAstPart)tmStack[tmHead - 1].value), 0, -1, source, tmLeft.offset, tmLeft.endoffset); 
				break;
			case 6:  // part ::= primitive_part '+'
				 tmLeft.value = new RegexAstQuantifier(((RegexAstPart)tmStack[tmHead - 1].value), 1, -1, source, tmLeft.offset, tmLeft.endoffset); 
				break;
			case 7:  // part ::= primitive_part '?'
				 tmLeft.value = new RegexAstQuantifier(((RegexAstPart)tmStack[tmHead - 1].value), 0, 1, source, tmLeft.offset, tmLeft.endoffset); 
				break;
			case 8:  // part ::= primitive_part quantifier
				 tmLeft.value = RegexUtil.createQuantifier(((RegexAstPart)tmStack[tmHead - 1].value), source, tmStack[tmHead].offset, tmLeft.endoffset, reporter); 
				break;
			case 9:  // primitive_part ::= char
				 tmLeft.value = new RegexAstChar(((Integer)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); 
				break;
			case 10:  // primitive_part ::= escaped
				 tmLeft.value = new RegexAstChar(((Integer)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); 
				break;
			case 11:  // primitive_part ::= charclass
				 tmLeft.value = new RegexAstCharClass(((String)tmStack[tmHead].value), RegexUtil.getClassSet(((String)tmStack[tmHead].value), setbuilder, reporter, tmLeft.offset, tmLeft.endoffset), source, tmLeft.offset, tmLeft.endoffset); 
				break;
			case 12:  // primitive_part ::= '.'
				 tmLeft.value = new RegexAstAny(source, tmLeft.offset, tmLeft.endoffset); 
				break;
			case 13:  // primitive_part ::= '(' pattern ')'
				 tmLeft.value = RegexUtil.wrap(((RegexAstPart)tmStack[tmHead - 1].value)); 
				break;
			case 14:  // primitive_part ::= '[' charset ']'
				 tmLeft.value = RegexUtil.toSet(((List<RegexAstPart>)tmStack[tmHead - 1].value), reporter, setbuilder, false); 
				break;
			case 15:  // primitive_part ::= '[^' charset ']'
				 tmLeft.value = RegexUtil.toSet(((List<RegexAstPart>)tmStack[tmHead - 1].value), reporter, setbuilder, true); 
				break;
			case 16:  // primitive_part ::= expand
				 tmLeft.value = new RegexAstExpand(source, tmLeft.offset, tmLeft.endoffset); RegexUtil.checkExpand((RegexAstExpand) tmLeft.value, reporter); 
				break;
			case 17:  // setsymbol ::= char
				 tmLeft.value = new RegexAstChar(((Integer)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); 
				break;
			case 18:  // setsymbol ::= escaped
				 tmLeft.value = new RegexAstChar(((Integer)tmStack[tmHead].value), source, tmLeft.offset, tmLeft.endoffset); 
				break;
			case 19:  // setsymbol ::= charclass
				 tmLeft.value = new RegexAstCharClass(((String)tmStack[tmHead].value), RegexUtil.getClassSet(((String)tmStack[tmHead].value), setbuilder, reporter, tmLeft.offset, tmLeft.endoffset), source, tmLeft.offset, tmLeft.endoffset); 
				break;
			case 20:  // charset ::= '-'
				 tmLeft.value = new ArrayList<RegexAstPart>(); ((List<RegexAstPart>)tmLeft.value).add(new RegexAstChar('-', source, tmStack[tmHead].offset, tmStack[tmHead].endoffset)); 
				break;
			case 21:  // charset ::= setsymbol
				 tmLeft.value = new ArrayList<RegexAstPart>(); RegexUtil.addSetSymbol(((List<RegexAstPart>)tmLeft.value), ((RegexAstPart)tmStack[tmHead].value), reporter); 
				break;
			case 22:  // charset ::= charset setsymbol
				 RegexUtil.addSetSymbol(((List<RegexAstPart>)tmStack[tmHead - 1].value), ((RegexAstPart)tmStack[tmHead].value), reporter); 
				break;
			case 23:  // charset ::= charset '-' %prio char
				 ((List<RegexAstPart>)tmStack[tmHead - 1].value).add(new RegexAstChar('-', source, tmStack[tmHead].offset, tmStack[tmHead].endoffset)); 
				break;
			case 24:  // charset ::= charset '-' char
				 RegexUtil.applyRange(((List<RegexAstPart>)tmStack[tmHead - 2].value), new RegexAstChar(((Integer)tmStack[tmHead].value), source, tmStack[tmHead].offset, tmStack[tmHead].endoffset), reporter); 
				break;
			case 25:  // charset ::= charset '-' escaped
				 RegexUtil.applyRange(((List<RegexAstPart>)tmStack[tmHead - 2].value), new RegexAstChar(((Integer)tmStack[tmHead].value), source, tmStack[tmHead].offset, tmStack[tmHead].endoffset), reporter); 
				break;
			case 27:  // parts ::= parts part
				 tmLeft.value = RegexUtil.createSequence(((RegexAstPart)tmStack[tmHead - 1].value), ((RegexAstPart)tmStack[tmHead].value)); 
				break;
		}
	}
}
