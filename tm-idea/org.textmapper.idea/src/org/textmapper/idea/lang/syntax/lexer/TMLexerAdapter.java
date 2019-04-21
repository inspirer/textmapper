/**
 * Copyright 2010-2017 Evgeny Gryaznov
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
			case Tokens.Rem:
				return OP_PERCENT;
			case Tokens.Dollar:
				return OP_DOLLAR;
			case Tokens.Or:
				return OP_OR;
			case Tokens.OrOr:
				return OP_OROR;
			case Tokens.Assign:
				return OP_EQ;
			case Tokens.AssignAssign:
				return OP_EQEQ;
			case Tokens.ExclAssign:
				return OP_EXCLEQ;
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
			case Tokens.Lbrack:
				return OP_LBRACKET;
			case Tokens.Rbrack:
				return OP_RBRACKET;
			case Tokens.Lparen:
				return OP_LPAREN;
			case Tokens.LparenQuestAssign:
				return OP_LPAREN_QA;
			case Tokens.Rparen:
				return OP_RPAREN;
			case Tokens.Lbrace:
				return OP_LCURLY;
			case Tokens.Rbrace:
				return OP_RCURLY;
			case Tokens.Lt:
				return OP_LT;
			case Tokens.Gt:
				return OP_GT;
			case Tokens.Mult:
				return OP_STAR;
			case Tokens.Plus:
				return OP_PLUS;
			case Tokens.PlusAssign:
				return OP_PLUSEQ;
			case Tokens.Quest:
				return OP_QMARK;
			case Tokens.Excl:
				return OP_EMARK;
			case Tokens.MinusGt:
				return OP_ARROW;
			case Tokens.And:
				return OP_AND;
			case Tokens.AndAnd:
				return OP_ANDAND;
			case Tokens.Atsign:
				return OP_AT;
			case Tokens.Tilde:
				return OP_TILDE;
			case Tokens.Div:
				return OP_DIV;

			// keywords
			case Tokens._true:
				return KW_TRUE;
			case Tokens._false:
				return KW_FALSE;
			case Tokens.separator:
				return KW_SEPARATOR;
			case Tokens.as:
				return KW_AS;
			case Tokens._import:
				return KW_IMPORT;
			case Tokens.set:
				return KW_SET;
			case Tokens._implements:
				return KW_IMPLEMENTS;

			// soft keywords
			case Tokens.brackets:
				return KW_BRACKETS;
			case Tokens.char_s:
				return KW_S;
			case Tokens.char_x:
				return KW_X;
			case Tokens.inline:
				return KW_INLINE;

			case Tokens.prec:
				return KW_PREC;
			case Tokens.shift:
				return KW_SHIFT;
			case Tokens.returns:
				return KW_RETURNS;

			case Tokens.input:
				return KW_INPUT;
			case Tokens.left:
				return KW_LEFT;
			case Tokens.right:
				return KW_RIGHT;
			case Tokens.nonassoc:
				return KW_NONASSOC;

			case Tokens.generate:
				return KW_GENERATE;
			case Tokens._assert:
				return KW_ASSERT;
			case Tokens.empty:
				return KW_EMPTY;
			case Tokens.nonempty:
				return KW_NONEMPTY;

			case Tokens.explicit:
				return KW_EXPLICIT;
			case Tokens.global:
				return KW_GLOBAL;
			case Tokens.lookahead:
				return KW_LOOKAHEAD;
			case Tokens.param:
				return KW_PARAM;
			case Tokens.flag:
				return KW_FLAG;

			case Tokens.noMinuseoi:
				return KW_NOEOI;

			case Tokens._class:
				return KW_CLASS;
			case Tokens._interface:
				return KW_INTERFACE;
			case Tokens._void:
				return KW_VOID;
			case Tokens.space:
				return KW_SPACE;

			case Tokens.layout:
				return KW_LAYOUT;
			case Tokens.language:
				return KW_LANGUAGE;
			case Tokens.lalr:
				return KW_LALR;

			case Tokens.lexer:
				return fState == STATE_AFTER_COLONCOLON ? KW_LEXER_ACC : KW_LEXER;
			case Tokens.parser:
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
		public IdeaLapgLexer(CharSequence input) throws IOException {
			super(input, (message, line, offset, endoffset) -> {
			});
		}

		@Override
		public void setState(int state) {
			afterColonColon = (state == STATE_AFTER_COLONCOLON);
			if (afterColonColon) {
				state = States.initial;
			}
			inStatesSelector = (state&16) != 0;
			if (inStatesSelector) {
				state &= ~16;
			}
			super.setState(state);
		}

		@Override
		public int getState() {
			if (afterColonColon) return STATE_AFTER_COLONCOLON;
			return super.getState() + (inStatesSelector ? 16 : 0);
		}

		@Override
		protected boolean createToken(Span token, int ruleIndex) throws IOException {
			super.createToken(token, ruleIndex);
			return true;
		}
	}
}
