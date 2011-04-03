/**
 * Copyright 2002-2010 Evgeny Gryaznov
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
package org.textway.lapg.eval;

import org.textway.lapg.api.Grammar;
import org.textway.lapg.api.SourceElement;
import org.textway.lapg.lex.LexerTables;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;

/**
 * Gryaznov Evgeny, 3/17/11
 */
public class GenericLexer {

	public static class ParseSymbol {
		public Object sym;
		public int lexem;
		public int state;
		public int line;
		public int offset;
		public int endoffset;
	}

	public interface ErrorReporter {
		void error(int start, int end, int line, String s);
	}

	public static final int TOKEN_SIZE = 2048;

	private Reader stream;
	final private ErrorReporter reporter;
	private final Grammar grammar;

	final private char[] data = new char[2048];
	private int datalen, l;
	private char chr;

	private int group;

	final private StringBuilder token = new StringBuilder(TOKEN_SIZE);

	private int tokenLine = 1;
	private int currLine = 1;
	private int currOffset = 0;

	private final int[] lapg_char2no;
	private final int[] lapg_lexemnum;
	private final int[][] lapg_lexem;


	public GenericLexer(Reader stream, ErrorReporter reporter, LexerTables tables, Grammar grammar) throws IOException {
		this.reporter = reporter;
		this.grammar = grammar;
		this.lapg_char2no = tables.char2no;
		this.lapg_lexem = tables.change;
		this.lapg_lexemnum = tables.lnum;

		reset(stream);
	}

	public void reset(Reader stream) throws IOException {
		this.stream = stream;
		this.datalen = stream.read(data);
		this.l = 0;
		this.group = 0;
		chr = l < datalen ? data[l++] : 0;
	}

	public int getState() {
		return group;
	}

	public void setState(int state) {
		this.group = state;
	}

	public int getTokenLine() {
		return tokenLine;
	}

	public int getLine() {
		return currLine;
	}

	public void setLine(int currLine) {
		this.currLine = currLine;
	}

	public int getOffset() {
		return currOffset;
	}

	public void setOffset(int currOffset) {
		this.currOffset = currOffset;
	}

	public String current() {
		return token.toString();
	}

	private int mapCharacter(int chr) {
		if (chr >= 0 && chr < 128) {
			return lapg_char2no[chr];
		}
		return 1;
	}

	public ParseSymbol next() throws IOException {
		ParseSymbol lapg_n = new ParseSymbol();
		int state;

		do {
			lapg_n.offset = currOffset;
			tokenLine = lapg_n.line = currLine;
			if (token.length() > TOKEN_SIZE) {
				token.setLength(TOKEN_SIZE);
				token.trimToSize();
			}
			token.setLength(0);
			int tokenStart = l - 1;

			for (state = group; state >= 0;) {
				state = lapg_lexem[state][mapCharacter(chr)];
				if (state >= -1 && chr != 0) {
					currOffset++;
					if (chr == '\n') {
						currLine++;
					}
					if (l >= datalen) {
						token.append(data, tokenStart, l - tokenStart);
						datalen = stream.read(data);
						tokenStart = l = 0;
					}
					chr = l < datalen ? data[l++] : 0;
				}
			}
			lapg_n.endoffset = currOffset;

			if (state == -1) {
				if (chr == 0) {
					reporter.error(lapg_n.offset, lapg_n.endoffset, currLine, "Unexpected end of file reached");
					break;
				}
				if (l - 1 > tokenStart) {
					token.append(data, tokenStart, l - 1 - tokenStart);
				}
				reporter.error(lapg_n.offset, lapg_n.endoffset, currLine, MessageFormat.format("invalid lexem at line {0}: `{1}`, skipped", currLine, current()));
				lapg_n.lexem = -1;
				continue;
			}

			if (state == -2) {
				lapg_n.lexem = 0;
				lapg_n.sym = null;
				return lapg_n;
			}

			if (l - 1 > tokenStart) {
				token.append(data, tokenStart, l - 1 - tokenStart);
			}

			lapg_n.lexem = lapg_lexemnum[-state - 3];
			lapg_n.sym = null;

		} while (lapg_n.lexem == -1 || !createToken(lapg_n, -state - 3));
		return lapg_n;
	}

	protected boolean createToken(ParseSymbol lapg_n, int lexemIndex) {
		SourceElement action = grammar.getLexems()[lexemIndex].getAction();
		if(action != null) {
			String text = action.getText();
			if(text.indexOf("return false") >= 0) {
				return false;
			}
		}
		return true;
	}
}
