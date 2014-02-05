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
package org.textmapper.idea.lang.templates.lexer;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.textmapper.templates.ast.TemplatesLexer;
import org.textmapper.templates.ast.TemplatesLexer.LapgSymbol;
import org.textmapper.templates.ast.TemplatesLexer.Lexems;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * Gryaznov Evgeny, 3/1/12
 */
public class LtplLexerAdapter extends LexerBase implements LtplTokenTypes {

	private CharSequence myText;
	private TemplatesLexer lexer;
	private LapgSymbol lexem;
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
		Reader reader = new StringReader(buffer.toString().substring(startOffset, endOffset));

		try {
			if (lexer == null) {
				lexer = new IdeaLtplLexer(reader);
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
			case Lexems.identifier:
				return IDENTIFIER;
			case Lexems.any:
				return ANY;
			case Lexems.escdollar:
				return OP_ESCDOLLAR;
			case Lexems.escid:
				return ESCID;
			case Lexems.escint:
				return ESCINT;
			case Lexems.DollarLcurly:
				return OP_DOLLARLCURLY;
			case Lexems.DollarSlash:
				return OP_DOLLARSLASH;
			case Lexems.icon:
				return ICON;
			case Lexems.ccon:
				return CCON;

			// keywords
			case Lexems.Lcall:
				return KW_CALL;
			case Lexems.Lcached:
				return KW_CACHED;
			case Lexems.Lcase:
				return KW_CASE;
			case Lexems.Lend:
				return KW_END;
			case Lexems.Lelse:
				return KW_ELSE;
			case Lexems.Leval:
				return KW_EVAL;
			case Lexems.Lfalse:
				return KW_FALSE;
			case Lexems.Lfor:
				return KW_FOR;
			case Lexems.Lfile:
				return KW_FILE;
			case Lexems.Lforeach:
				return KW_FOREACH;
			case Lexems.Lgrep:
				return KW_GREP;
			case Lexems.Lif:
				return KW_IF;
			case Lexems.Lin:
				return KW_IN;
			case Lexems.Limport:
				return KW_IMPORT;
			case Lexems.Lis:
				return KW_IS;
			case Lexems.Lmap:
				return KW_MAP;
			case Lexems.Lnew:
				return KW_NEW;
			case Lexems.Lnull:
				return KW_NULL;
			case Lexems.Lquery:
				return KW_QUERY;
			case Lexems.Lswitch:
				return KW_SWITCH;
			case Lexems.Lseparator:
				return KW_SEPARATOR;
			case Lexems.Ltemplate:
				return KW_TEMPLATE;
			case Lexems.Ltrue:
				return KW_TRUE;
			case Lexems.Lself:
				return KW_SELF;
			case Lexems.Lassert:
				return KW_ASSERT;

			// operators
			case Lexems.Lcurly:
				return OP_LCURLY;
			case Lexems.Rcurly:
				return OP_RCURLY;
			case Lexems.MinusRcurly:
				return OP_MINUSRCURLY;
			case Lexems.Plus:
				return OP_PLUS;
			case Lexems.Minus:
				return OP_MINUS;
			case Lexems.Mult:
				return OP_MULT;
			case Lexems.Slash:
				return OP_SLASH;
			case Lexems.Percent:
				return OP_PERCENT;
			case Lexems.Exclamation:
				return OP_EXCLAMATION;
			case Lexems.Or:
				return OP_OR;
			case Lexems.Lsquare:
				return OP_LSQUARE;
			case Lexems.Rsquare:
				return OP_RSQUARE;
			case Lexems.Lparen:
				return OP_LPAREN;
			case Lexems.Rparen:
				return OP_RPAREN;
			case Lexems.Dot:
				return OP_DOT;
			case Lexems.Comma:
				return OP_COMMA;
			case Lexems.AmpersandAmpersand:
				return OP_AMPERSANDAMPERSAND;
			case Lexems.OrOr:
				return OP_OROR;
			case Lexems.EqualEqual:
				return OP_EQUALEQUAL;
			case Lexems.Equal:
				return OP_EQUAL;
			case Lexems.ExclamationEqual:
				return OP_EXCLAMATIONEQUAL;
			case Lexems.MinusGreater:
				return OP_MINUSGREATER;
			case Lexems.EqualGreater:
				return OP_EQUALGREATER;
			case Lexems.LessEqual:
				return OP_LESSEQUAL;
			case Lexems.GreaterEqual:
				return OP_GREATEREQUAL;
			case Lexems.Less:
				return OP_LESS;
			case Lexems.Greater:
				return OP_GREATER;
			case Lexems.Colon:
				return OP_COLON;
			case Lexems.Questionmark:
				return OP_QUESTIONMARK;
			case Lexems._skip:
				return WHITESPACE;
		}

		/* default, eoi */
		lexem = currentLexem;
		assert lexem.symbol == Lexems.eoi && lexem.endoffset == fDocumentLength;
		return null;
	}

	private void readNext() {
		try {
			lexem = lexer.next();
		} catch (IOException e) {
			/* never happens */
		}
	}

	private static class IdeaLtplLexer extends TemplatesLexer {
		public IdeaLtplLexer(Reader stream) throws IOException {
			super(stream, new ErrorReporter() {
				@Override
				public void error(String message, int line, int offset, int endoffset) {
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
