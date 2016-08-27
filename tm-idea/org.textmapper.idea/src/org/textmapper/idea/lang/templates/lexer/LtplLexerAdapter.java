/**
 * Copyright (c) 2010-2016 Evgeny Gryaznov
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
package org.textmapper.idea.lang.templates.lexer;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.textmapper.templates.ast.TemplatesLexer;
import org.textmapper.templates.ast.TemplatesLexer.Span;
import org.textmapper.templates.ast.TemplatesLexer.Tokens;

import java.io.IOException;

/**
 * Gryaznov Evgeny, 3/1/12
 */
public class LtplLexerAdapter extends LexerBase implements LtplTokenTypes {

	private CharSequence myText;
	private TemplatesLexer lexer;
	private Span token;
	private int fDocumentLength;
	private int fTokenOffset;
	private int fState;
	private int fTokenLength;
	private IElementType current;

	public LtplLexerAdapter() {
	}

	public void start(final CharSequence buffer, int startOffset, int endOffset, int initialState) {
		myText = buffer;
		fDocumentLength = endOffset;
		String input = buffer.toString().substring(startOffset, endOffset);

		try {
			if (lexer == null) {
				lexer = new IdeaLtplLexer(input);
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

	public int getState() {
		locateToken();
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
			case Tokens.identifier:
				return IDENTIFIER;
			case Tokens.any:
				return ANY;
			case Tokens.escdollar:
				return OP_ESCDOLLAR;
			case Tokens.escid:
				return ESCID;
			case Tokens.escint:
				return ESCINT;
			case Tokens.DollarLbrace:
				return OP_DOLLARLCURLY;
			case Tokens.DollarDiv:
				return OP_DOLLARSLASH;
			case Tokens.icon:
				return ICON;
			case Tokens.ccon:
				return CCON;

			// keywords
			case Tokens.Lcall:
				return KW_CALL;
			case Tokens.Lcached:
				return KW_CACHED;
			case Tokens.Lcase:
				return KW_CASE;
			case Tokens.Lend:
				return KW_END;
			case Tokens.Lelse:
				return KW_ELSE;
			case Tokens.Leval:
				return KW_EVAL;
			case Tokens.Lfalse:
				return KW_FALSE;
			case Tokens.Lfor:
				return KW_FOR;
			case Tokens.Lfile:
				return KW_FILE;
			case Tokens.Lforeach:
				return KW_FOREACH;
			case Tokens.Lgrep:
				return KW_GREP;
			case Tokens.Lif:
				return KW_IF;
			case Tokens.Lin:
				return KW_IN;
			case Tokens.Limport:
				return KW_IMPORT;
			case Tokens.Lis:
				return KW_IS;
			case Tokens.Lmap:
				return KW_MAP;
			case Tokens.Lnew:
				return KW_NEW;
			case Tokens.Lnull:
				return KW_NULL;
			case Tokens.Lquery:
				return KW_QUERY;
			case Tokens.Lswitch:
				return KW_SWITCH;
			case Tokens.Lseparator:
				return KW_SEPARATOR;
			case Tokens.Ltemplate:
				return KW_TEMPLATE;
			case Tokens.Ltrue:
				return KW_TRUE;
			case Tokens.Lself:
				return KW_SELF;
			case Tokens.Lassert:
				return KW_ASSERT;

			// operators
			case Tokens.Lbrace:
				return OP_LCURLY;
			case Tokens.Rbrace:
				return OP_RCURLY;
			case Tokens.MinusRbrace:
				return OP_MINUSRCURLY;
			case Tokens.Plus:
				return OP_PLUS;
			case Tokens.Minus:
				return OP_MINUS;
			case Tokens.Mult:
				return OP_MULT;
			case Tokens.Div:
				return OP_SLASH;
			case Tokens.Rem:
				return OP_PERCENT;
			case Tokens.Excl:
				return OP_EXCLAMATION;
			case Tokens.Or:
				return OP_OR;
			case Tokens.Lbrack:
				return OP_LSQUARE;
			case Tokens.Rbrack:
				return OP_RSQUARE;
			case Tokens.Lparen:
				return OP_LPAREN;
			case Tokens.Rparen:
				return OP_RPAREN;
			case Tokens.Dot:
				return OP_DOT;
			case Tokens.Comma:
				return OP_COMMA;
			case Tokens.AndAnd:
				return OP_AMPERSANDAMPERSAND;
			case Tokens.OrOr:
				return OP_OROR;
			case Tokens.AssignAssign:
				return OP_EQUALEQUAL;
			case Tokens.Assign:
				return OP_EQUAL;
			case Tokens.ExclAssign:
				return OP_EXCLAMATIONEQUAL;
			case Tokens.MinusGt:
				return OP_MINUSGREATER;
			case Tokens.AssignGt:
				return OP_EQUALGREATER;
			case Tokens.LtAssign:
				return OP_LESSEQUAL;
			case Tokens.GtAssign:
				return OP_GREATEREQUAL;
			case Tokens.Lt:
				return OP_LESS;
			case Tokens.Gt:
				return OP_GREATER;
			case Tokens.Colon:
				return OP_COLON;
			case Tokens.Quest:
				return OP_QUESTIONMARK;
			case Tokens._skip:
				return WHITESPACE;
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

	private static class IdeaLtplLexer extends TemplatesLexer {
		public IdeaLtplLexer(CharSequence input) throws IOException {
			super(input, (message, line, offset, endoffset) -> {
			});
		}

		@Override
		protected boolean createToken(Span token, int ruleIndex) throws IOException {
			super.createToken(token, ruleIndex);
			return true;
		}
	}
}
