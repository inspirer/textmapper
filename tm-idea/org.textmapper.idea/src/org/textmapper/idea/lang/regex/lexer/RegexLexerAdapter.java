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
package org.textmapper.idea.lang.regex.lexer;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.tree.IElementType;
import org.textmapper.lapg.regex.RegexDefLexer;
import org.textmapper.lapg.regex.RegexDefLexer.LapgSymbol;
import org.textmapper.lapg.regex.RegexDefLexer.Tokens;

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
	private int fRegexpStartOffset;
	private IElementType current;

	public RegexLexerAdapter() {
	}

	@Override
	public void start(final CharSequence buffer, int startOffset, int endOffset, int initialState) {
		myText = buffer;
		fDocumentLength = endOffset;
		Reader reader = new StringReader(buffer.subSequence(startOffset, endOffset).toString());

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
		fRegexpStartOffset = initialState == 0 ? startOffset : -1;
		lexer.setState(initialState > 0 ? initialState - 1 : 0);
		fState = initialState;
		fTokenLength = 0;
		lexem = null;
		current = null;
	}

	@Override
	public int getState() {
		locateToken();
		if (fTokenOffset == fRegexpStartOffset) {
			return 0;
		}
		return fState;
	}

	@Override
	public IElementType getTokenType() {
		locateToken();
		return current;
	}

	@Override
	public int getTokenStart() {
		locateToken();
		return fTokenOffset;
	}

	@Override
	public int getTokenEnd() {
		locateToken();
		return fTokenOffset + fTokenLength;
	}

	@Override
	public void advance() {
		locateToken();
		current = null;
	}

	@Override
	public CharSequence getBufferSequence() {
		return myText;
	}

	@Override
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
			fState = lexer.getState() + 1;
			readNext();
		}
		if (fTokenOffset < lexem.offset) {
			fTokenLength = lexem.offset - fTokenOffset;
			if (fTokenOffset == fRegexpStartOffset && myText.charAt(fTokenOffset) == '/') {
				fTokenLength = 1;
				return RE_DELIMITERS;
			}
			if (lexem.symbol == Tokens.eoi && fTokenLength > 1 && myText.charAt(lexem.offset - 1) == '/') {
				fTokenLength--;
				return RE_BAD;
			}
			if (lexem.symbol == Tokens.eoi && fTokenLength == 1 && myText.charAt(fTokenOffset) == '/') {
				return RE_DELIMITERS;
			}
			return RE_BAD;
		}
		int token = lexem.symbol;
		fTokenLength = lexem.endoffset - fTokenOffset;
		LapgSymbol currentLexem = lexem;
		lexem = null;
		switch (token) {
			case Tokens._char:
				return RE_CHAR;
			case Tokens.escaped:
				return RE_ESCAPED;
			case Tokens.charclass:
				return RE_CHARCLASS;

			case Tokens.Dot:
				return RE_DOT;
			case Tokens.Mult:
				return RE_MULT;
			case Tokens.Plus:
				return RE_PLUS;
			case Tokens.Questionmark:
				return RE_QUESTIONMARK;
			case Tokens.quantifier:
				return RE_QUANTFIER;

			case Tokens.Lparen:
				return RE_LPAREN;
			case Tokens.LparenQuestionmark:
				return RE_LPARENQMARK;
			case Tokens.Or:
				return RE_OR;
			case Tokens.Rparen:
				return RE_RPAREN;
			case Tokens.expand:
				return RE_EXPAND;

			case Tokens.Lsquare:
				return RE_LSQUARE;
			case Tokens.LsquareXor:
				return RE_LSQUAREXOR;
			case Tokens.Minus:
				return RE_MINUS;
			case Tokens.Rsquare:
				return RE_RSQUARE;

			case Tokens.kw_eoi:
				return RE_EOI;
			case Tokens.op_intersect:
				return RE_INTERSECT;
			case Tokens.op_minus:
				return RE_SETDIFF;
			case Tokens.op_union:
				return RE_SETUNION;
		}

		/* default, eoi */
		lexem = currentLexem;
		assert lexem.symbol == Tokens.eoi && lexem.endoffset == fDocumentLength;
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
				@Override
				public void error(String message, int offset, int endoffset) {
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
