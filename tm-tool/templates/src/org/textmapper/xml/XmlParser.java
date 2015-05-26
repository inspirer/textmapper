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
package org.textmapper.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.text.MessageFormat;
import org.textmapper.xml.XmlLexer.ErrorReporter;
import org.textmapper.xml.XmlLexer.Span;
import org.textmapper.xml.XmlLexer.Tokens;
import org.textmapper.xml.XmlTree.TextSource;

public class XmlParser {

	public static class ParseException extends Exception {
		private static final long serialVersionUID = 1L;

		public ParseException() {
		}
	}

	private final ErrorReporter reporter;

	public XmlParser(ErrorReporter reporter) {
		this.reporter = reporter;
	}


	private static final boolean DEBUG_SYNTAX = false;

	TextSource source;

	private XmlData getData(int start, int end) {
		return new XmlData(source.getContents(), start, end-start);
	}

	private void checkTag(XmlNode node, String endTag, int offset, int endoffset, int line) {
		if (!node.getTagName().equals(endTag))
			reporter.error("Tag " + node.getTagName() + " is closed with " + endTag, line, offset, endoffset);
	}
	private static final int[] tmAction = XmlLexer.unpack_int(31,
		"\uffff\uffff\6\0\uffff\uffff\ufffd\uffff\2\0\uffff\uffff\5\0\ufff5\uffff\uffeb\uffff" +
		"\1\0\uffff\uffff\uffff\uffff\3\0\uffff\uffff\uffff\uffff\uffe3\uffff\15\0\uffff\uffff" +
		"\uffff\uffff\4\0\10\0\uffff\uffff\14\0\11\0\uffff\uffff\uffff\uffff\16\0\12\0\13" +
		"\0\uffff\uffff\ufffe\uffff");

	private static final short[] tmLalr = XmlLexer.unpack_short(34,
		"\1\uffff\2\uffff\0\0\uffff\ufffe\10\uffff\4\7\6\7\11\7\uffff\ufffe\4\uffff\6\20\11" +
		"\20\uffff\ufffe\4\uffff\6\17\11\17\uffff\ufffe");

	private static final short[] lapg_sym_goto = XmlLexer.unpack_short(22,
		"\0\1\5\11\11\17\20\23\24\25\27\27\30\32\36\41\45\51\53\54\56\57");

	private static final short[] lapg_sym_from = XmlLexer.unpack_short(47,
		"\35\0\3\5\13\0\3\5\13\2\10\12\15\17\22\25\21\30\31\16\7\12\21\0\0\5\0\3\5\13\2\12" +
		"\22\0\3\5\13\0\3\5\13\5\13\10\10\17\10");

	private static final short[] lapg_sym_to = XmlLexer.unpack_short(47,
		"\36\1\1\1\1\2\2\12\12\7\16\7\24\16\7\32\27\33\34\25\15\22\30\35\3\13\4\11\4\11\10" +
		"\10\31\5\5\5\5\6\6\6\6\14\23\17\20\26\21");

	private static final short[] tmRuleLen = XmlLexer.unpack_short(17,
		"\1\2\1\2\3\1\1\1\3\4\5\4\2\1\3\1\0");

	private static final short[] tmRuleSymbol = XmlLexer.unpack_short(17,
		"\13\14\14\15\15\15\15\16\16\17\20\21\22\22\23\24\24");

	protected static final String[] tmSymbolNames = new String[] {
		"eoi",
		"any",
		"'<'",
		"_skipcomment",
		"identifier",
		"ccon",
		"'>'",
		"'='",
		"':'",
		"'/'",
		"_skip",
		"input",
		"xml_tags",
		"xml_tag_or_space",
		"tag_name",
		"tag_start",
		"no_body_tag",
		"tag_end",
		"attributes",
		"attribute",
		"attributesopt",
	};

	public interface Nonterminals extends Tokens {
		// non-terminals
		static final int input = 11;
		static final int xml_tags = 12;
		static final int xml_tag_or_space = 13;
		static final int tag_name = 14;
		static final int tag_start = 15;
		static final int no_body_tag = 16;
		static final int tag_end = 17;
		static final int attributes = 18;
		static final int attribute = 19;
		static final int attributesopt = 20;
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
	protected XmlLexer tmLexer;

	public XmlNode parse(XmlLexer lexer) throws IOException, ParseException {

		tmLexer = lexer;
		tmStack = new Span[1024];
		tmHead = 0;

		tmStack[0] = new Span();
		tmStack[0].state = 0;
		tmNext = tmLexer.next();

		while (tmStack[tmHead].state != 30) {
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

		if (tmStack[tmHead].state != 30) {
			reporter.error(MessageFormat.format("syntax error before line {0}",
								tmLexer.getTokenLine()), tmNext.line, tmNext.offset, tmNext.endoffset);
			throw new ParseException();
		}
		return (XmlNode)tmStack[tmHead - 1].value;
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
		left.line = startsym.line;
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
			case 0:  // input ::= xml_tags
				{ tmLeft.value = new XmlNode("<root>", null, 1); ((XmlNode)tmLeft.value).setData(((List<XmlElement>)tmStack[tmHead].value)); }
				break;
			case 1:  // xml_tags ::= xml_tags xml_tag_or_space
				{ ((List<XmlElement>)tmStack[tmHead - 1].value).add(((XmlElement)tmStack[tmHead].value)); }
				break;
			case 2:  // xml_tags ::= xml_tag_or_space
				{ tmLeft.value = new ArrayList<XmlElement>(); ((List<XmlElement>)tmLeft.value).add(((XmlElement)tmStack[tmHead].value)); }
				break;
			case 3:  // xml_tag_or_space ::= tag_start tag_end
				{ checkTag(((XmlNode)tmStack[tmHead - 1].value),((String)tmStack[tmHead].value),tmStack[tmHead].offset,tmStack[tmHead].endoffset,tmStack[tmHead].line); }
				break;
			case 4:  // xml_tag_or_space ::= tag_start xml_tags tag_end
				{ checkTag(((XmlNode)tmStack[tmHead - 2].value),((String)tmStack[tmHead].value),tmStack[tmHead].offset,tmStack[tmHead].endoffset,tmStack[tmHead].line); ((XmlNode)tmStack[tmHead - 2].value).setData(((List<XmlElement>)tmStack[tmHead - 1].value)); }
				break;
			case 6:  // xml_tag_or_space ::= any
				{ tmLeft.value = getData(tmLeft.offset,tmLeft.endoffset); }
				break;
			case 7:  // tag_name ::= identifier
				{ tmLeft.value = ((String)tmStack[tmHead].value); }
				break;
			case 8:  // tag_name ::= identifier ':' identifier
				{ tmLeft.value = ((String)tmStack[tmHead - 2].value) + ":" + ((String)tmStack[tmHead].value); }
				break;
			case 9:  // tag_start ::= '<' tag_name attributesopt '>'
				{ tmLeft.value = new XmlNode(((String)tmStack[tmHead - 2].value), ((List<XmlAttribute>)tmStack[tmHead - 1].value), tmStack[tmHead - 3].line); }
				break;
			case 10:  // no_body_tag ::= '<' tag_name attributesopt '/' '>'
				{ tmLeft.value = new XmlNode(((String)tmStack[tmHead - 3].value), ((List<XmlAttribute>)tmStack[tmHead - 2].value), tmStack[tmHead - 4].line); }
				break;
			case 11:  // tag_end ::= '<' '/' tag_name '>'
				{ tmLeft.value = ((String)tmStack[tmHead - 1].value); }
				break;
			case 12:  // attributes ::= attributes attribute
				{ ((List<XmlAttribute>)tmStack[tmHead - 1].value).add(((XmlAttribute)tmStack[tmHead].value)); }
				break;
			case 13:  // attributes ::= attribute
				{ tmLeft.value = new ArrayList<XmlAttribute>(); ((List<XmlAttribute>)tmLeft.value).add(((XmlAttribute)tmStack[tmHead].value)); }
				break;
			case 14:  // attribute ::= identifier '=' ccon
				{ tmLeft.value = new XmlAttribute(((String)tmStack[tmHead - 2].value),((String)tmStack[tmHead].value)); }
				break;
		}
	}
}
