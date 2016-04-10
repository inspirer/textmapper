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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
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

	private static final int[] tmLalr = XmlLexer.unpack_int(34,
		"\1\0\uffff\uffff\2\0\uffff\uffff\0\0\0\0\uffff\uffff\ufffe\uffff\10\0\uffff\uffff" +
		"\4\0\7\0\6\0\7\0\11\0\7\0\uffff\uffff\ufffe\uffff\4\0\uffff\uffff\6\0\20\0\11\0\20" +
		"\0\uffff\uffff\ufffe\uffff\4\0\uffff\uffff\6\0\17\0\11\0\17\0\uffff\uffff\ufffe\uffff");

	private static final int[] lapg_sym_goto = XmlLexer.unpack_int(22,
		"\0\0\1\0\5\0\11\0\11\0\17\0\20\0\23\0\24\0\25\0\27\0\27\0\30\0\32\0\36\0\41\0\45" +
		"\0\51\0\53\0\54\0\56\0\57\0");

	private static final int[] lapg_sym_from = XmlLexer.unpack_int(47,
		"\35\0\0\0\3\0\5\0\13\0\0\0\3\0\5\0\13\0\2\0\10\0\12\0\15\0\17\0\22\0\25\0\21\0\30" +
		"\0\31\0\16\0\7\0\12\0\21\0\0\0\0\0\5\0\0\0\3\0\5\0\13\0\2\0\12\0\22\0\0\0\3\0\5\0" +
		"\13\0\0\0\3\0\5\0\13\0\5\0\13\0\10\0\10\0\17\0\10\0");

	private static final int[] lapg_sym_to = XmlLexer.unpack_int(47,
		"\36\0\1\0\1\0\1\0\1\0\2\0\2\0\12\0\12\0\7\0\16\0\7\0\24\0\16\0\7\0\32\0\27\0\33\0" +
		"\34\0\25\0\15\0\22\0\30\0\35\0\3\0\13\0\4\0\11\0\4\0\11\0\10\0\10\0\31\0\5\0\5\0" +
		"\5\0\5\0\6\0\6\0\6\0\6\0\14\0\23\0\17\0\20\0\26\0\21\0");

	private static final int[] tmRuleLen = XmlLexer.unpack_int(17,
		"\1\0\2\0\1\0\2\0\3\0\1\0\1\0\1\0\3\0\4\0\5\0\4\0\2\0\1\0\3\0\1\0\0\0");

	private static final int[] tmRuleSymbol = XmlLexer.unpack_int(17,
		"\13\0\14\0\14\0\15\0\15\0\15\0\15\0\16\0\16\0\17\0\20\0\21\0\22\0\22\0\23\0\24\0" +
		"\24\0");

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
		int input = 11;
		int xml_tags = 12;
		int xml_tag_or_space = 13;
		int tag_name = 14;
		int tag_start = 15;
		int no_body_tag = 16;
		int tag_end = 17;
		int attributes = 18;
		int attribute = 19;
		int attributesopt = 20;
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
