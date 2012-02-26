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
	private static final int lapg_action[] = {
		-1, 6, -1, -3, 2, -1, 5, -11, -21, 1, -1, -1, 3, -1, -1, -29,
		15, -1, -1, 4, 8, -1, 14, 11, -1, -1, 16, 12, 13, -1, -2
	};

	private static final short lapg_lalr[] = {
		1, -1, 2, -1, 0, 0, -1, -2, 8, -1, 4, 7, 6, 7, 9, 7,
		-1, -2, 4, -1, 6, 9, 9, 9, -1, -2, 4, -1, 6, 10, 9, 10,
		-1, -2
	};

	private static final short lapg_sym_goto[] = {
		0, 1, 5, 9, 9, 15, 16, 19, 20, 21, 23, 23, 24, 26, 30, 33,
		37, 41, 43, 44, 46, 47
	};

	private static final short lapg_sym_from[] = {
		29, 0, 3, 5, 11, 0, 3, 5, 11, 2, 8, 10, 13, 15, 18, 21,
		17, 24, 25, 14, 7, 10, 17, 0, 0, 5, 0, 3, 5, 11, 2, 10,
		18, 0, 3, 5, 11, 0, 3, 5, 11, 5, 11, 8, 8, 15, 8
	};

	private static final short lapg_sym_to[] = {
		30, 1, 1, 1, 1, 2, 2, 10, 10, 7, 14, 7, 20, 14, 7, 26,
		23, 27, 28, 21, 13, 18, 24, 29, 3, 11, 4, 9, 4, 9, 8, 8,
		25, 5, 5, 5, 5, 6, 6, 6, 6, 12, 19, 15, 16, 22, 17
	};

	private static final short lapg_rlen[] = {
		1, 2, 1, 2, 3, 1, 1, 1, 3, 0, 1, 4, 5, 4, 2, 1,
		3
	};

	private static final short lapg_rlex[] = {
		11, 12, 12, 13, 13, 13, 13, 14, 14, 20, 20, 15, 16, 17, 18, 18,
		19
	};

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

	protected static final int lapg_state_sym(int state, int symbol) {
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
		lapg_m[lapg_head].state = lapg_state_sym(lapg_m[lapg_head-1].state, lapg_gg.lexem);
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
				 checkTag(((XmlNode)lapg_m[lapg_head-1].sym),((String)lapg_m[lapg_head].sym),lapg_m[lapg_head].offset,lapg_m[lapg_head].endoffset,lapg_m[lapg_head].line); 
				break;
			case 4:  // xml_tag_or_space ::= tag_start xml_tags tag_end
				 checkTag(((XmlNode)lapg_m[lapg_head-2].sym),((String)lapg_m[lapg_head].sym),lapg_m[lapg_head].offset,lapg_m[lapg_head].endoffset,lapg_m[lapg_head].line); ((XmlNode)lapg_m[lapg_head-2].sym).setData(((List<XmlElement>)lapg_m[lapg_head-1].sym)); 
				break;
			case 6:  // xml_tag_or_space ::= any
				 lapg_gg.sym = getData(lapg_m[lapg_head].offset,lapg_m[lapg_head].endoffset); 
				break;
			case 7:  // tag_name ::= identifier
				 lapg_gg.sym = ((String)lapg_m[lapg_head].sym); 
				break;
			case 8:  // tag_name ::= identifier ':' identifier
				 lapg_gg.sym = ((String)lapg_m[lapg_head-2].sym) + ":" + ((String)lapg_m[lapg_head].sym); 
				break;
			case 11:  // tag_start ::= '<' tag_name attributesopt '>'
				 lapg_gg.sym = new XmlNode(((String)lapg_m[lapg_head-2].sym), ((List<XmlAttribute>)lapg_m[lapg_head-1].sym), lapg_m[lapg_head-3].line); 
				break;
			case 12:  // no_body_tag ::= '<' tag_name attributesopt '/' '>'
				 lapg_gg.sym = new XmlNode(((String)lapg_m[lapg_head-3].sym), ((List<XmlAttribute>)lapg_m[lapg_head-2].sym), lapg_m[lapg_head-4].line); 
				break;
			case 13:  // tag_end ::= '<' '/' tag_name '>'
				 lapg_gg.sym = ((String)lapg_m[lapg_head-1].sym); 
				break;
			case 14:  // attributes ::= attributes attribute
				 ((List<XmlAttribute>)lapg_gg.sym).add(((XmlAttribute)lapg_m[lapg_head].sym)); 
				break;
			case 15:  // attributes ::= attribute
				 lapg_gg.sym = new ArrayList<XmlAttribute>(); ((List<XmlAttribute>)lapg_gg.sym).add(((XmlAttribute)lapg_m[lapg_head].sym)); 
				break;
			case 16:  // attribute ::= identifier '=' ccon
				 lapg_gg.sym = new XmlAttribute(((String)lapg_m[lapg_head-2].sym),((String)lapg_m[lapg_head].sym)); 
				break;
		}
	}
}
