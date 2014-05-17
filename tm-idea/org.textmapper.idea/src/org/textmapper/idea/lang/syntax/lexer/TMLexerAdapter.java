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
package org.textmapper.idea.lang.syntax.lexer;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.textmapper.tool.parser.TMLexer;
import org.textmapper.tool.parser.TMLexer.LapgSymbol;
import org.textmapper.tool.parser.TMLexer.Tokens;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class TMLexerAdapter extends LexerBase implements TMTokenTypes {

	public static final int STATE_AFTER_COLONCOLON = 1000;

	private CharSequence myText;
	private TMLexer lexer;
	private LapgSymbol lexem;
	private int fDocumentLength;
	private int fTokenOffset;
	private int fTokenLength;
	private int fState;
	private IElementType current;

	public TMLexerAdapter() {
	}

	@Override
	public void start(final CharSequence buffer, int startOffset, int endOffset, int initialState) {
		myText = buffer;
		fDocumentLength = endOffset;
		Reader reader = new StringReader(buffer.toString().substring(startOffset, endOffset));

		try {
			if (lexer == null) {
				lexer = new IdeaLapgLexer(reader);
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

	@Override
	public int getState() {
		locateToken();
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
			fState = lexer.getState();
			readNext();
		}
		if (fTokenOffset < lexem.offset) {
			fTokenLength = lexem.offset - fTokenOffset;
			return TokenType.BAD_CHARACTER;
		}
		int token = lexem.symbol;
		fTokenLength = lexem.endoffset - fTokenOffset;
		LapgSymbol currentLexem = lexem;
		lexem = null;
		switch (token) {
			case Tokens.code:
				return TOKEN_ACTION;
			case Tokens._skip:
				return WHITESPACE;
			case Tokens._skip_comment:
				return COMMENT;
			case Tokens.scon:
				return STRING;
			case Tokens.icon:
				return ICON;
			case Tokens.ID:
				return ID;
			case Tokens.regexp:
				return REGEXP;

			// operators
			case Tokens.Percent:
				return OP_PERCENT;
			case Tokens.Dollar:
				return OP_DOLLAR;
			case Tokens.DotDot:
				return OP_DOTDOT;
			case Tokens.ColonColonEqual:
				return OP_CCEQ;
			case Tokens.Or:
				return OP_OR;
			case Tokens.Equal:
				return OP_EQ;
			case Tokens.EqualGreater:
				return OP_EQGT;
			case Tokens.Semicolon:
				return OP_SEMICOLON;
			case Tokens.Dot:
				return OP_DOT;
			case Tokens.Comma:
				return OP_COMMA;
			case Tokens.Colon:
				return OP_COLON;
			case Tokens.ColonColon:
				return OP_COLONCOLON;
			case Tokens.Lsquare:
				return OP_LBRACKET;
			case Tokens.Rsquare:
				return OP_RBRACKET;
			case Tokens.Lparen:
				return OP_LPAREN;
			case Tokens.Rparen:
				return OP_RPAREN;
			case Tokens.Lcurly:
				return OP_LCURLY;
			case Tokens.Rcurly:
				return OP_RCURLY;
			case Tokens.Less:
				return OP_LT;
			case Tokens.Greater:
				return OP_GT;
			case Tokens.Mult:
				return OP_STAR;
			case Tokens.Plus:
				return OP_PLUS;
			case Tokens.PlusEqual:
				return OP_PLUSEQ;
			case Tokens.Questionmark:
				return OP_QMARK;
//			case Tokens.MINUSGREATER:
//				return OP_ARROW;
			case Tokens.Ampersand:
				return OP_AND;
			case Tokens.Atsign:
				return OP_AT;
			case Tokens.Tilde:
				return OP_TILDE;

			// keywords
			case Tokens.Ltrue:
				return KW_TRUE;
			case Tokens.Lfalse:
				return KW_FALSE;
			case Tokens.Lseparator:
				return KW_SEPARATOR;
			case Tokens.Lprio:
				return KW_PRIO;
			case Tokens.Lshift:
				return KW_SHIFT;
			case Tokens.Lreduce:
				return KW_REDUCE;
			case Tokens.Linput:
				return KW_INPUT;
			case Tokens.Lleft:
				return KW_LEFT;
			case Tokens.Lright:
				return KW_RIGHT;
			case Tokens.Lnew:
				return KW_NEW;
			case Tokens.Lnonassoc:
				return KW_NONASSOC;
			case Tokens.Lnoeoi:
				return KW_NOEOI;
			case Tokens.Las:
				return KW_AS;
			case Tokens.Limport:
				return KW_IMPORT;
			case Tokens.Lset:
				return KW_SET;
			case Tokens.Linline:
				return KW_INLINE;
			case Tokens.Lreturns:
				return KW_RETURNS;
			case Tokens.Linterface:
				return KW_INTERFACE;
			case Tokens.Lvoid:
				return KW_VOID;
			case Tokens.Llanguage:
				return KW_LANGUAGE;
			case Tokens.Llalr:
				return KW_LALR;
			case Tokens.Llexer:
				return fState == STATE_AFTER_COLONCOLON ? KW_LEXER_ACC : KW_LEXER;
			case Tokens.Lparser:
				return fState == STATE_AFTER_COLONCOLON ? KW_PARSER_ACC : KW_PARSER;

			// soft keywords without highlighting
			case Tokens.Lsoft:
				return KW_SOFT;
			case Tokens.Lclass:
				return KW_CLASS;
			case Tokens.Lspace:
				return KW_SPACE;
		}

		/* default, eoi */
		lexem = currentLexem;
		assert lexem.symbol == Tokens.eoi;
		if (lexem.offset < fDocumentLength) {
			fTokenLength = fDocumentLength - fTokenOffset;
			lexem.offset = lexem.endoffset = fDocumentLength;
			return TEMPLATES;
		}
		return null;
	}

	private void readNext() {
		try {
			lexem = lexer.next();
		} catch (IOException e) {
			/* never happens */
		}
	}

	private static class IdeaLapgLexer extends TMLexer {
		private boolean fAfterColonColon = false;

		public IdeaLapgLexer(Reader stream) throws IOException {
			super(stream, new ErrorReporter() {
				@Override
				public void error(String message, int line, int offset, int endoffset) {
				}
			});
		}

		@Override
		public void setState(int state) {
			fAfterColonColon = (state == STATE_AFTER_COLONCOLON);
			super.setState(fAfterColonColon ? States.initial : state);
		}

		@Override
		public int getState() {
			return fAfterColonColon ? STATE_AFTER_COLONCOLON : super.getState();
		}

		@Override
		public void reset(Reader stream) throws IOException {
			fAfterColonColon = false;
			super.reset(stream);
		}

		@Override
		protected boolean createToken(LapgSymbol lapg_n, int lexemIndex) throws IOException {
			super.createToken(lapg_n, lexemIndex);
			return true;
		}

		@Override
		public LapgSymbol next() throws IOException {
			LapgSymbol next = super.next();
			if (next.symbol != Tokens._skip && next.symbol != Tokens._skip_comment) {
				fAfterColonColon = (next.symbol == Tokens.ColonColon && super.getState() == States.initial);
			}
			return next;
		}
	}
}
