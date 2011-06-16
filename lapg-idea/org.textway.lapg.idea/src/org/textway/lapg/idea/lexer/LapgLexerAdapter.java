/**
 * Copyright 2002-2011 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.textway.lapg.idea.lexer;

import com.intellij.lexer.LexerBase;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.textway.lapg.parser.LapgLexer;
import org.textway.lapg.parser.LapgLexer.LapgSymbol;
import org.textway.lapg.parser.LapgLexer.Lexems;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class LapgLexerAdapter extends LexerBase implements LapgTokenTypes {

	private CharSequence myText;
	private LapgLexer lexer;
	private LapgSymbol lexem;
	private int fDocumentLength;
	private int fTokenOffset;
	private int fTokenLength;
	private IElementType current;

	public LapgLexerAdapter() {
	}

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
		fTokenLength = 0;
		lexem = null;
		current = null;
	}

	public int getState() {
		return 0;
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
			readNext();
		}
		if (fTokenOffset < lexem.offset) {
			fTokenLength = lexem.offset - fTokenOffset;
			return TokenType.BAD_CHARACTER;
		}
		int token = lexem.lexem;
		if (token == Lexems.LCURLY) {
			skipAction();
			fTokenLength = lexem.endoffset - fTokenOffset;
			lexem = null;
			return TOKEN_ACTION;
		}
		fTokenLength = lexem.endoffset - fTokenOffset;
		LapgSymbol currentLexem = lexem;
		lexem = null;
		switch (token) {
			case Lexems._skip:
				return WHITESPACE;
			case Lexems._skip_comment:
				return COMMENT;
			case Lexems.scon:
				return STRING;
			case Lexems.icon:
				return ICON;
			case Lexems.identifier:
				return IDENTIFIER;
			case Lexems.regexp:
				return REGEXP;

			// operators
			case Lexems.PERCENT:
				return OP_PERCENT;
			case Lexems.COLONCOLONEQUAL:
				return OP_CCEQ;
			case Lexems.OR:
				return OP_OR;
			case Lexems.EQUAL:
				return OP_EQ;
			case Lexems.EQUALGREATER:
				return OP_EQGT;
			case Lexems.SEMICOLON:
				return OP_SEMICOLON;
			case Lexems.DOT:
				return OP_DOT;
			case Lexems.COMMA:
				return OP_COMMA;
			case Lexems.COLON:
				return OP_COLON;
			case Lexems.LSQUARE:
				return OP_LBRACKET;
			case Lexems.RSQUARE:
				return OP_RBRACKET;
			case Lexems.LPAREN:
				return OP_LPAREN;
			case Lexems.RPAREN:
				return OP_RPAREN;
			case Lexems.LESS:
				return OP_LT;
			case Lexems.GREATER:
				return OP_GT;
			case Lexems.MULT:
				return OP_STAR;
			case Lexems.PLUS:
				return OP_PLUS;
			case Lexems.QUESTIONMARK:
				return OP_QMARK;
			case Lexems.AMPERSAND:
				return OP_AND;
			case Lexems.ATSIGN:
				return OP_AT;

			// keywords
			case Lexems.Ltrue:
				return KW_TRUE;
			case Lexems.Lfalse:
				return KW_FALSE;
			case Lexems.Lprio:
				return KW_PRIO;
			case Lexems.Lshift:
				return KW_SHIFT;
			case Lexems.Lreduce:
				return KW_REDUCE;
			case Lexems.Linput:
				return KW_INPUT;
			case Lexems.Lleft:
				return KW_LEFT;
			case Lexems.Lright:
				return KW_RIGHT;
			case Lexems.Lnonassoc:
				return KW_NONASSOC;
			case Lexems.Lnoeoi:
				return KW_NOEOI;
		}

		/* default, eoi */
		lexem = currentLexem;
		assert lexem.lexem == Lexems.eoi;
		if (lexem.endoffset < fDocumentLength) {
			fTokenLength = fDocumentLength - fTokenOffset;
			lexem.offset = lexem.endoffset = fDocumentLength;
			return TEMPLATES;
		}
		return null;
	}

	private void skipAction() {
		int deep = 1;
		while (lexem.lexem != Lexems.eoi && deep > 0) {
			readNext();
			switch (lexem.lexem) {
				case Lexems.iLCURLY:
				case Lexems.LCURLY:
					deep++;
					break;
				case Lexems.RCURLY:
					deep--;
					break;
			}
		}
	}

	private void readNext() {
		try {
			lexem = lexer.next();
		} catch (IOException e) {
			/* never happens */
		}
	}

	private static class IdeaLapgLexer extends LapgLexer {
		public IdeaLapgLexer(Reader stream) throws IOException {
			super(stream, new ErrorReporter() {
				public void error(int start, int end, int line, String s) {
				}
			});
		}

		@Override
		protected boolean createToken(LapgSymbol lapg_n, int lexemIndex) {
			super.createToken(lapg_n, lexemIndex);
			return true;
		}
	}
}
