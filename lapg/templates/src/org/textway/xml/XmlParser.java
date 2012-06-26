/**
 * Copyright 2002-2012 Evgeny Gryaznov
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
package org.textway.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.text.MessageFormat;
import org.textway.xml.XmlLexer.ErrorReporter;
import org.textway.xml.XmlLexer.LapgSymbol;
import org.textway.xml.XmlLexer.Lexems;
import org.textway.xml.XmlTree.TextSource;

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
			reporter.error(offset, endoffset, line, "Tag " + node.getTagName() + " is closed with " + endTag);
	}
	private static final int[] lapg_action = XmlLexer.unpack_int(31,
		"\uffff\uffff\6\0\uffff\uffff\ufffd\uffff\2\0\uffff\uffff\5\0\ufff5\uffff\uffeb\uffff" +
		"\1\0\uffff\uffff\uffff\uffff\3\0\uffff\uffff\uffff\uffff\uffe3\uffff\17\0\uffff\uffff" +
		"\uffff\uffff\4\0\10\0\uffff\uffff\16\0\13\0\uffff\uffff\uffff\uffff\20\0\14\0\15" +
		"\0\uffff\uffff\ufffe\uffff");

	private static final short[] lapg_lalr = XmlLexer.unpack_short(34,
		"\1\uffff\2\uffff\0\0\uffff\ufffe\10\uffff\4\7\6\7\11\7\uffff\ufffe\4\uffff\6\11\11" +
		"\11\uffff\ufffe\4\uffff\6\12\11\12\uffff\ufffe");

	private static final short[] lapg_sym_goto = XmlLexer.unpack_short(22,
		"\0\1\5\11\11\17\20\23\24\25\27\27\30\32\36\41\45\51\53\54\56\57");

	private static final short[] lapg_sym_from = XmlLexer.unpack_short(47,
		"\35\0\3\5\13\0\3\5\13\2\10\12\15\17\22\25\21\30\31\16\7\12\21\0\0\5\0\3\5\13\2\12" +
		"\22\0\3\5\13\0\3\5\13\5\13\10\10\17\10");

	private static final short[] lapg_sym_to = XmlLexer.unpack_short(47,
		"\36\1\1\1\1\2\2\12\12\7\16\7\24\16\7\32\27\33\34\25\15\22\30\35\3\13\4\11\4\11\10" +
		"\10\31\5\5\5\5\6\6\6\6\14\23\17\20\26\21");

	private static final short[] lapg_rlen = XmlLexer.unpack_short(17,
		"\1\2\1\2\3\1\1\1\3\0\1\4\5\4\2\1\3");

	private static final short[] lapg_rlex = XmlLexer.unpack_short(17,
		"\13\14\14\15\15\15\15\16\16\24\24\17\20\21\22\22\23");

	protected static final String[] lapg_syms = new String[] {
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

	public interface Tokens extends Lexems {
		// non-terminals
		public static final int input = 11;
		public static final int xml_tags = 12;
		public static final int xml_tag_or_space = 13;
		public static final int tag_name = 14;
		public static final int tag_start = 15;
		public static final int no_body_tag = 16;
		public static final int tag_end = 17;
		public static final int attributes = 18;
		public static final int attribute = 19;
		public static final int attributesopt = 20;
	}

	protected final int lapg_next(int state) {
		int p;
		if (lapg_action[state] < -2) {
			for (p = -lapg_action[state] - 3; lapg_lalr[p] >= 0; p += 2) {
				if (lapg_lalr[p] == lapg_n.lexem) {
					break;
				}
			}
			return lapg_lalr[p + 1];
		}
		return lapg_action[state];
	}

	protected final int lapg_state_sym(int state, int symbol) {
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

	protected int lapg_head;
	protected LapgSymbol[] lapg_m;
	protected LapgSymbol lapg_n;
	protected XmlLexer lapg_lexer;

	public XmlNode parse(XmlLexer lexer) throws IOException, ParseException {

		lapg_lexer = lexer;
		lapg_m = new LapgSymbol[1024];
		lapg_head = 0;

		lapg_m[0] = new LapgSymbol();
		lapg_m[0].state = 0;
		lapg_n = lapg_lexer.next();

		while (lapg_m[lapg_head].state != 30) {
			int lapg_i = lapg_next(lapg_m[lapg_head].state);

			if (lapg_i >= 0) {
				reduce(lapg_i);
			} else if (lapg_i == -1) {
				shift();
			}

			if (lapg_i == -2 || lapg_m[lapg_head].state == -1) {
				break;
			}
		}

		if (lapg_m[lapg_head].state != 30) {
			reporter.error(lapg_n.offset, lapg_n.endoffset, lapg_n.line,
						MessageFormat.format("syntax error before line {0}",
								lapg_lexer.getTokenLine()));
			throw new ParseException();
		}
		return (XmlNode)lapg_m[lapg_head - 1].sym;
	}

	protected void shift() throws IOException {
		lapg_m[++lapg_head] = lapg_n;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, lapg_n.lexem);
		if (DEBUG_SYNTAX) {
			System.out.println(MessageFormat.format("shift: {0} ({1})", lapg_syms[lapg_n.lexem], lapg_lexer.current()));
		}
		if (lapg_m[lapg_head].state != -1 && lapg_n.lexem != 0) {
			lapg_n = lapg_lexer.next();
		}
	}

	protected void reduce(int rule) {
		LapgSymbol lapg_gg = new LapgSymbol();
		lapg_gg.sym = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head + 1 - lapg_rlen[rule]].sym : null;
		lapg_gg.lexem = lapg_rlex[rule];
		lapg_gg.state = 0;
		if (DEBUG_SYNTAX) {
			System.out.println("reduce to " + lapg_syms[lapg_rlex[rule]]);
		}
		LapgSymbol startsym = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head + 1 - lapg_rlen[rule]] : lapg_n;
		lapg_gg.line = startsym.line;
		lapg_gg.offset = startsym.offset;
		lapg_gg.endoffset = (lapg_rlen[rule] != 0) ? lapg_m[lapg_head].endoffset : lapg_n.offset;
		applyRule(lapg_gg, rule, lapg_rlen[rule]);
		for (int e = lapg_rlen[rule]; e > 0; e--) {
			lapg_m[lapg_head--] = null;
		}
		lapg_m[++lapg_head] = lapg_gg;
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head - 1].state, lapg_gg.lexem);
	}

	@SuppressWarnings("unchecked")
	protected void applyRule(LapgSymbol lapg_gg, int rule, int ruleLength) {
		switch (rule) {
			case 0:  // input ::= xml_tags
				 lapg_gg.sym = new XmlNode("<root>", null, 1); ((XmlNode)lapg_gg.sym).setData(((List<XmlElement>)lapg_m[lapg_head].sym)); 
				break;
			case 1:  // xml_tags ::= xml_tags xml_tag_or_space
				 ((List<XmlElement>)lapg_gg.sym).add(((XmlElement)lapg_m[lapg_head].sym)); 
				break;
			case 2:  // xml_tags ::= xml_tag_or_space
				 lapg_gg.sym = new ArrayList<XmlElement>(); ((List<XmlElement>)lapg_gg.sym).add(((XmlElement)lapg_m[lapg_head].sym)); 
				break;
			case 3:  // xml_tag_or_space ::= tag_start tag_end
				 checkTag(((XmlNode)lapg_m[lapg_head - 1].sym),((String)lapg_m[lapg_head].sym),lapg_m[lapg_head].offset,lapg_m[lapg_head].endoffset,lapg_m[lapg_head].line); 
				break;
			case 4:  // xml_tag_or_space ::= tag_start xml_tags tag_end
				 checkTag(((XmlNode)lapg_m[lapg_head - 2].sym),((String)lapg_m[lapg_head].sym),lapg_m[lapg_head].offset,lapg_m[lapg_head].endoffset,lapg_m[lapg_head].line); ((XmlNode)lapg_m[lapg_head - 2].sym).setData(((List<XmlElement>)lapg_m[lapg_head - 1].sym)); 
				break;
			case 6:  // xml_tag_or_space ::= any
				 lapg_gg.sym = getData(lapg_m[lapg_head].offset,lapg_m[lapg_head].endoffset); 
				break;
			case 7:  // tag_name ::= identifier
				 lapg_gg.sym = ((String)lapg_m[lapg_head].sym); 
				break;
			case 8:  // tag_name ::= identifier ':' identifier
				 lapg_gg.sym = ((String)lapg_m[lapg_head - 2].sym) + ":" + ((String)lapg_m[lapg_head].sym); 
				break;
			case 11:  // tag_start ::= '<' tag_name attributesopt '>'
				 lapg_gg.sym = new XmlNode(((String)lapg_m[lapg_head - 2].sym), ((List<XmlAttribute>)lapg_m[lapg_head - 1].sym), lapg_m[lapg_head - 3].line); 
				break;
			case 12:  // no_body_tag ::= '<' tag_name attributesopt '/' '>'
				 lapg_gg.sym = new XmlNode(((String)lapg_m[lapg_head - 3].sym), ((List<XmlAttribute>)lapg_m[lapg_head - 2].sym), lapg_m[lapg_head - 4].line); 
				break;
			case 13:  // tag_end ::= '<' '/' tag_name '>'
				 lapg_gg.sym = ((String)lapg_m[lapg_head - 1].sym); 
				break;
			case 14:  // attributes ::= attributes attribute
				 ((List<XmlAttribute>)lapg_gg.sym).add(((XmlAttribute)lapg_m[lapg_head].sym)); 
				break;
			case 15:  // attributes ::= attribute
				 lapg_gg.sym = new ArrayList<XmlAttribute>(); ((List<XmlAttribute>)lapg_gg.sym).add(((XmlAttribute)lapg_m[lapg_head].sym)); 
				break;
			case 16:  // attribute ::= identifier '=' ccon
				 lapg_gg.sym = new XmlAttribute(((String)lapg_m[lapg_head - 2].sym),((String)lapg_m[lapg_head].sym)); 
				break;
		}
	}
}
