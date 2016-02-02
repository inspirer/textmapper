/**
 * Copyright (c) 2010-2012 Evgeny Gryaznov
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package org.textmapper.idea.lang.syntax.lexer;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.textmapper.tool.parser.TMLexer;
import org.textmapper.tool.parser.TMLexer.Span;
import org.textmapper.tool.parser.TMLexer.Tokens;

import java.io.IOException;

public class TMLexerAdapter extends LexerBase implements TMTokenTypes {

	public static final int STATE_AFTER_COLONCOLON = 1000;

	private CharSequence myText;
	private TMLexer lexer;
	private Span token;
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
		String input = buffer.toString().substring(startOffset, endOffset);

		try {
			if (lexer == null) {
				lexer = new IdeaLapgLexer(input);
			} else {
				lexer.reset(input);
			}
		} catch (IOException ex) {
			/* never happens */
		}
		lexer.setOffset(startOffset);
		fTokenOffset = startOffset;
		lexer.setState(initialState);
		fState = initialState;
		fTokenLength = 0;
		token = null;
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
		if (token == null) {
			fState = lexer.getState();
			readNext();
		}
		if (fTokenOffset < token.offset) {
			fTokenLength = token.offset - fTokenOffset;
			return TokenType.BAD_CHARACTER;
		}
		int symbol = token.symbol;
		fTokenLength = token.endoffset - fTokenOffset;
		Span currentToken = token;
		token = null;
		switch (symbol) {
			case Tokens.code:
				return TOKEN_ACTION;
			case Tokens._skip:
				return WHITESPACE;
			case Tokens._skip_comment:
				return COMMENT;
			case Tokens._skip_multiline:
				return ML_COMMENT;
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
			case Tokens.ColonColonEqual:
				return OP_CCEQ;
			case Tokens.Or:
				return OP_OR;
			case Tokens.OrOr:
				return OP_OROR;
			case Tokens.Equal:
				return OP_EQ;
			case Tokens.EqualEqual:
				return OP_EQEQ;
			case Tokens.ExclamationEqual:
				return OP_EXCLEQ;
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
			case Tokens.LcurlyTilde:
				return OP_LCURLYTILDE;
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
			case Tokens.Exclamation:
				return OP_EMARK;
//			case Tokens.MINUSGREATER:
//				return OP_ARROW;
			case Tokens.Ampersand:
				return OP_AND;
			case Tokens.AmpersandAmpersand:
				return OP_ANDAND;
			case Tokens.Atsign:
				return OP_AT;
			case Tokens.Tilde:
				return OP_TILDE;

			// keywords
			case Tokens.Ltrue:
				return KW_TRUE;
			case Tokens.Lfalse:
				return KW_FALSE;
			case Tokens.Lnew:
				return KW_NEW;
			case Tokens.Lseparator:
				return KW_SEPARATOR;
			case Tokens.Las:
				return KW_AS;
			case Tokens.Limport:
				return KW_IMPORT;
			case Tokens.Lset:
				return KW_SET;

			// soft keywords
			case Tokens.Lbrackets:
				return KW_BRACKETS;
			case Tokens.Linline:
				return KW_INLINE;

			case Tokens.Lprec:
				return KW_PREC;
			case Tokens.Lshift:
				return KW_SHIFT;
			case Tokens.Lreduce:
				return KW_REDUCE;
			case Tokens.Lreturns:
				return KW_RETURNS;

			case Tokens.Linput:
				return KW_INPUT;
			case Tokens.Lleft:
				return KW_LEFT;
			case Tokens.Lright:
				return KW_RIGHT;
			case Tokens.Lnonassoc:
				return KW_NONASSOC;

			case Tokens.Lgenerate:
				return KW_GENERATE;
			case Tokens.Lassert:
				return KW_ASSERT;
			case Tokens.Lempty:
				return KW_EMPTY;
			case Tokens.Lnonempty:
				return KW_NONEMPTY;

			case Tokens.Lglobal:
				return KW_GLOBAL;
			case Tokens.Lparam:
				return KW_PARAM;
			case Tokens.Lflag:
				return KW_FLAG;

			case Tokens.Lnoeoi:
				return KW_NOEOI;

			case Tokens.Lsoft:
				return KW_SOFT;
			case Tokens.Lclass:
				return KW_CLASS;
			case Tokens.Linterface:
				return KW_INTERFACE;
			case Tokens.Lvoid:
				return KW_VOID;
			case Tokens.Lspace:
				return KW_SPACE;

			case Tokens.Llayout:
				return KW_LAYOUT;
			case Tokens.Llanguage:
				return KW_LANGUAGE;
			case Tokens.Llalr:
				return KW_LALR;

			case Tokens.Llexer:
				return fState == STATE_AFTER_COLONCOLON ? KW_LEXER_ACC : KW_LEXER;
			case Tokens.Lparser:
				return fState == STATE_AFTER_COLONCOLON ? KW_PARSER_ACC : KW_PARSER;
		}

		/* default, eoi */
		token = currentToken;
		assert token.symbol == Tokens.eoi;
		if (token.offset < fDocumentLength) {
			fTokenLength = fDocumentLength - fTokenOffset;
			token.offset = token.endoffset = fDocumentLength;
			return TEMPLATES;
		}
		return null;
	}

	private void readNext() {
		try {
			token = lexer.next();
		} catch (IOException e) {
			/* never happens */
		}
	}

	private static class IdeaLapgLexer extends TMLexer {
		private boolean fAfterColonColon = false;

		public IdeaLapgLexer(CharSequence input) throws IOException {
			super(input, (message, line, offset, endoffset) -> {
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
		public void reset(CharSequence input) throws IOException {
			fAfterColonColon = false;
			super.reset(input);
		}

		@Override
		protected boolean createToken(Span token, int ruleIndex) throws IOException {
			super.createToken(token, ruleIndex);
			return true;
		}

		@Override
		public Span next() throws IOException {
			Span next = super.next();
			if (next.symbol != Tokens._skip && next.symbol != Tokens._skip_comment) {
				fAfterColonColon = (next.symbol == Tokens.ColonColon && super.getState() == States.initial);
			}
			return next;
		}
	}
}
