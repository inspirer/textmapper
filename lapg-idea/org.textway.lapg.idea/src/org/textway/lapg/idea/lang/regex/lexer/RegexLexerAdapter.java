/**
 * Copyright (c) 2010-2012 Evgeny Gryaznov
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package org.textway.lapg.idea.lang.regex.lexer;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.textway.lapg.regex.RegexDefLexer;
import org.textway.lapg.regex.RegexDefLexer.LapgSymbol;
import org.textway.lapg.regex.RegexDefLexer.Lexems;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * evgeny, 3/4/12
 */
public class RegexLexerAdapter extends LexerBase implements RegexTokenTypes {

	private CharSequence myText;
	private RegexDefLexer lexer;
	private LapgSymbol lexem;
	private int fDocumentLength;
	private int fTokenOffset;
	private int fState;
	private int fTokenLength;
	private IElementType current;

	public RegexLexerAdapter() {
	}

	public void start(final CharSequence buffer, int startOffset, int endOffset, int initialState) {
		myText = buffer;
		fDocumentLength = endOffset;
		Reader reader = new StringReader(buffer.toString().substring(startOffset, endOffset));

		try {
			if (lexer == null) {
				lexer = new IdeaRegexDefLexer(reader);
			} else {
				lexer.reset(reader);
			}
		} catch (IOException ex) {
			/* never happens */
		}
		lexer.setOffset(startOffset);
		fTokenOffset = startOffset;
		lexer.setState(initialState);
		fState = initialState;
		fTokenLength = 0;
		lexem = null;
		current = null;
	}

	public int getState() {
		return fState;
	}

	public IElementType getTokenType() {
		locateToken();
		return current;
	}

	public int getTokenStart() {
		locateToken();
		return fTokenOffset;
	}

	public int getTokenEnd() {
		locateToken();
		return fTokenOffset + fTokenLength;
	}

	public void advance() {
		locateToken();
		current = null;
	}

	public CharSequence getBufferSequence() {
		return myText;
	}

	public int getBufferEnd() {
		return fDocumentLength;
	}

	private void locateToken() {
		if (current == null) {
			current = nextToken();
		}
	}

	public IElementType nextToken() {
		fTokenOffset += fTokenLength;
		if (lexem == null) {
			fState = lexer.getState();
			readNext();
		}
		if (fTokenOffset < lexem.offset) {
			fTokenLength = lexem.offset - fTokenOffset;
			return TokenType.BAD_CHARACTER;
		}
		int token = lexem.lexem;
		fTokenLength = lexem.endoffset - fTokenOffset;
		LapgSymbol currentLexem = lexem;
		lexem = null;
		switch (token) {
			case Lexems._char:
				return RE_CHAR;
			case Lexems.charclass:
				return RE_CHARCLASS;
			case Lexems.DOT:
				return RE_DOT;
			case Lexems.MINUS:
				return RE_MINUS;
			case Lexems.XOR:
				return RE_XOR;
			case Lexems.LPAREN:
				return RE_LPAREN;
			case Lexems.OR:
				return RE_OR;
			case Lexems.RPAREN:
				return RE_RPAREN;
			case Lexems.LCURLY:
				return RE_LCURLY;
			case Lexems.LCURLYdigit:
				return RE_LCURLYDIGIT;
			case Lexems.LCURLYletter:
				return RE_LCURLYLETTER;
			case Lexems.RCURLY:
				return RE_RCURLY;
			case Lexems.LSQUARE:
				return RE_LSQUARE;
			case Lexems.RSQUARE:
				return RE_RSQUARE;
			case Lexems.MULT:
				return RE_MULT;
			case Lexems.PLUS:
				return RE_PLUS;
			case Lexems.QUESTIONMARK:
				return RE_QUESTIONMARK;
		}

		/* default, eoi */
		lexem = currentLexem;
		assert lexem.lexem == Lexems.eoi && lexem.endoffset == fDocumentLength;
		return null;
	}

	private void readNext() {
		try {
			lexem = lexer.next();
		} catch (IOException e) {
			/* never happens */
		}
	}

	private static class IdeaRegexDefLexer extends RegexDefLexer {
		public IdeaRegexDefLexer(Reader stream) throws IOException {
			super(stream, new ErrorReporter() {
				public void error(int start, int end, int line, String s) {
				}
			});
		}

		@Override
		protected boolean createToken(LapgSymbol lapg_n, int lexemIndex) throws IOException {
			super.createToken(lapg_n, lexemIndex);
			return true;
		}
	}
}
