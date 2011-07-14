/**
 * Copyright 2002-2011 Evgeny Gryaznov
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
package org.textway.lapg.test.cases.bootstrap.b;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class SampleBLexer {

	public static class LapgSymbol {
		public Object sym;
		public int lexem;
		public int state;
		public int offset;
		public int endoffset;
	}

	public interface Lexems {
		public static final int eoi = 0;
		public static final int identifier = 1;
		public static final int _skip = 2;
		public static final int Lclass = 3;
		public static final int Lextends = 4;
		public static final int LCURLY = 5;
		public static final int RCURLY = 6;
		public static final int LPAREN = 7;
		public static final int RPAREN = 8;
		public static final int Linterface = 9;
		public static final int Lenum = 10;
		public static final int error = 11;
		public static final int numeric = 12;
		public static final int octal = 13;
		public static final int decimal = 14;
		public static final int eleven = 15;
		public static final int _skipSoftKW = 16;
	}

	public interface ErrorReporter {
		void error(int start, int end, int line, String s);
	}

	public static final int TOKEN_SIZE = 2048;

	private Reader stream;
	final private ErrorReporter reporter;

	final private char[] data = new char[2048];
	private int datalen, l;
	private char chr;

	private int group;

	final private StringBuilder token = new StringBuilder(TOKEN_SIZE);

	private int tokenLine = 1;
	private int currLine = 1;
	private int currOffset = 0;


	public SampleBLexer(Reader stream, ErrorReporter reporter) throws IOException {
		this.reporter = reporter;
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

	private static final short lapg_char2no[] = {
		0, 1, 1, 1, 1, 1, 1, 1, 1, 10, 10, 1, 1, 10, 1, 1,
		1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
		10, 1, 1, 1, 1, 1, 1, 1, 4, 5, 1, 1, 1, 1, 1, 1,
		6, 12, 12, 12, 12, 12, 12, 12, 9, 9, 1, 1, 1, 1, 1, 1,
		1, 11, 11, 11, 11, 11, 11, 8, 8, 8, 8, 8, 8, 8, 8, 8,
		8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 1, 1, 1, 1, 8,
		1, 11, 11, 11, 11, 11, 11, 8, 8, 8, 8, 8, 8, 8, 8, 8,
		8, 8, 8, 8, 8, 8, 8, 8, 7, 8, 8, 2, 1, 3, 1, 1
	};

	private static final short lapg_lexemnum[] = {
		1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 13, 14, 15, 16
	};

	private static final short[][] lapg_lexem = new short[][] {
		{ -2, -1, 1, 2, 3, 4, 5, 6, 6, 7, 8, 6, 7},
		{ -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7, -7},
		{ -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8, -8},
		{ -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9, -9},
		{ -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10, -10},
		{ -1, -1, -1, -1, -1, -1, 9, 10, -1, -1, -1, -1, 9},
		{ -3, -3, -3, -3, -3, -3, 6, 6, 6, 6, -3, 6, 6},
		{ -1, -1, -1, -1, -1, -1, 11, -1, -1, 11, -1, -1, 11},
		{ -4, -4, -4, -4, -4, -4, -4, -4, -4, -4, 8, -4, -4},
		{ -14, -14, -14, -14, -14, -14, 9, -14, -14, -14, -14, -14, 9},
		{ -1, -1, -1, -1, -1, -1, 12, -1, -1, 12, -1, 12, 12},
		{ -15, -15, -15, -15, -15, -15, 11, -15, -15, 11, -15, -15, 11},
		{ -13, -13, -13, -13, -13, -13, 12, -13, -13, 12, -13, 12, 12}
	};

	private static int mapCharacter(int chr) {
		if (chr >= 0 && chr < 128) {
			return lapg_char2no[chr];
		}
		return 1;
	}

	public LapgSymbol next() throws IOException {
		LapgSymbol lapg_n = new LapgSymbol();
		int state;

		do {
			lapg_n.offset = currOffset;
			tokenLine = currLine;
			if (token.length() > TOKEN_SIZE) {
				token.setLength(TOKEN_SIZE);
				token.trimToSize();
			}
			token.setLength(0);
			int tokenStart = l - 1;

			for (state = group; state >= 0;) {
				state = lapg_lexem[state][mapCharacter(chr)];
				if (state == -1 && chr == 0) {
					lapg_n.endoffset = currOffset;
					lapg_n.lexem = 0;
					lapg_n.sym = null;
					reporter.error(lapg_n.offset, lapg_n.endoffset, this.getTokenLine(), "Unexpected end of input reached");
					return lapg_n;
				}
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
				if (l - 1 > tokenStart) {
					token.append(data, tokenStart, l - 1 - tokenStart);
				}
				reporter.error(lapg_n.offset, lapg_n.endoffset, this.getTokenLine(), MessageFormat.format("invalid lexem at line {0}: `{1}`, skipped", currLine, current()));
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

	protected boolean createToken(LapgSymbol lapg_n, int lexemIndex) {
		switch (lexemIndex) {
			case 0:
				return createIdentifierToken(lapg_n, lexemIndex);
			case 1:
				 return false; 
			case 10:
				return createNumericToken(lapg_n, lexemIndex);
			case 11:
				return createOctalToken(lapg_n, lexemIndex);
			case 12:
				return createDecimalToken(lapg_n, lexemIndex);
		}
		return true;
	}

	private static Map<String,Integer> subTokensOfIdentifier = new HashMap<String,Integer>();
	static {
		subTokensOfIdentifier.put("class", 2);
		subTokensOfIdentifier.put("extends", 3);
		subTokensOfIdentifier.put("interface", 8);
		subTokensOfIdentifier.put("enum", 9);
		subTokensOfIdentifier.put("xyzzz", 14);
	}

	protected boolean createIdentifierToken(LapgSymbol lapg_n, int lexemIndex) {
		Integer replacement = subTokensOfIdentifier.get(current());
		if(replacement != null) {
			lexemIndex = replacement;
			lapg_n.lexem = lapg_lexemnum[lexemIndex];
		}
		switch(lexemIndex) {
			case 2:	// class
				 lapg_n.sym = "class"; break; 
			case 8:	// interface
				 lapg_n.sym = "interface"; break; 
			case 9:	// enum
				 lapg_n.sym = new Object(); break; 
			case 3:	// extends (soft)
			case 14:	// xyzzz (soft)
			case 0:	// <default>
				 lapg_n.sym = current(); break; 
		}
		return true;
	}

	protected boolean createNumericToken(LapgSymbol lapg_n, int lexemIndex) {
		return true;
	}

	protected boolean createOctalToken(LapgSymbol lapg_n, int lexemIndex) {
		switch(lexemIndex) {
			case 11:	// <default>
				 lapg_n.sym = Integer.parseInt(current(), 8); break; 
		}
		return true;
	}

	private static Map<String,Integer> subTokensOfDecimal = new HashMap<String,Integer>();
	static {
		subTokensOfDecimal.put("11", 13);
	}

	protected boolean createDecimalToken(LapgSymbol lapg_n, int lexemIndex) {
		Integer replacement = subTokensOfDecimal.get(current());
		if(replacement != null) {
			lexemIndex = replacement;
			lapg_n.lexem = lapg_lexemnum[lexemIndex];
		}
		switch(lexemIndex) {
			case 13:	// 11
				 lapg_n.sym = 11; break; 
		}
		return true;
	}
}
