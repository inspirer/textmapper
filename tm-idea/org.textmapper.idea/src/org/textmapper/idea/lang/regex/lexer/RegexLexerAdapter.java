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
import org.jetbrains.annotations.NotNull;
import org.textmapper.lapg.regex.RegexDefLexer;
import org.textmapper.lapg.regex.RegexDefLexer.Span;
import org.textmapper.lapg.regex.RegexDefLexer.Tokens;

import java.io.IOException;

/**
 * evgeny, 3/4/12
 */
public class RegexLexerAdapter extends LexerBase implements RegexTokenTypes {

	private CharSequence myText;
	private RegexDefLexer lexer;
	private Span token;
	private int fDocumentLength;
	private int fTokenOffset;
	private int fState;
	private int fTokenLength;
	private int fRegexpStartOffset;
	private IElementType current;

	public RegexLexerAdapter() {
	}

	@Override
	public void start(@NotNull final CharSequence buffer, int startOffset, int endOffset, int initialState) {
		myText = buffer;
		fDocumentLength = endOffset;
		CharSequence input = buffer.subSequence(startOffset, endOffset);

		try {
			if (lexer == null) {
				lexer = new IdeaRegexDefLexer(input);
			} else {
				lexer.reset(input);
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
		token = null;
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

	@NotNull
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
		if (token == null) {
			fState = lexer.getState() + 1;
			readNext();
		}
		if (fTokenOffset < token.offset) {
			fTokenLength = token.offset - fTokenOffset;
			if (fTokenOffset == fRegexpStartOffset && myText.charAt(fTokenOffset) == '/') {
				fTokenLength = 1;
				return RE_DELIMITERS;
			}
			if (token.symbol == Tokens.eoi && fTokenLength > 1 && myText.charAt(token.offset - 1) == '/') {
				fTokenLength--;
				return RE_BAD;
			}
			if (token.symbol == Tokens.eoi && fTokenLength == 1 && myText.charAt(fTokenOffset) == '/') {
				return RE_DELIMITERS;
			}
			return RE_BAD;
		}
		int symbol = token.symbol;
		fTokenLength = token.endoffset - fTokenOffset;
		Span currentToken = token;
		token = null;
		switch (symbol) {
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
		token = currentToken;
		assert token.symbol == Tokens.eoi && token.endoffset == fDocumentLength;
		return null;
	}

	private void readNext() {
		try {
			token = lexer.next();
		} catch (IOException e) {
			/* never happens */
		}
	}

	private static class IdeaRegexDefLexer extends RegexDefLexer {
		public IdeaRegexDefLexer(CharSequence input) throws IOException {
			super(input, (message, offset, endoffset) -> {
			});
		}

		@Override
		protected boolean createToken(Span token, int ruleIndex) throws IOException {
			super.createToken(token, ruleIndex);
			return true;
		}
	}
}
