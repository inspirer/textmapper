/**
 * Copyright 2002-2010 Evgeny Gryaznov
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
package net.sf.lapg.idea.lexer;

import com.intellij.lexer.LexerBase;
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
				lexer = new SkippingLapgLexer(reader);
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
		if(current == null) { current = nextToken(); }
	}

	public IElementType nextToken() {
		fTokenOffset += fTokenLength;
		if (lexem == null) {
			readNext();
		}
		if (fTokenOffset < lexem.offset) {
			fTokenLength = lexem.offset - fTokenOffset;
			return TEXT;
		}
		int token = lexem.lexem;
		switch (token) {
			case Lexems._skip_comment:
			case Lexems.scon:
			case Lexems.regexp:
				fTokenLength = lexem.endoffset - fTokenOffset;
				lexem = null;
				if (token == Lexems._skip_comment) {
					return COMMENT;
				} else if (token == Lexems.scon) {
					return STRING;
				} else {
					return REGEXP;
				}
			case Lexems.LCURLY:
				skipAction();
				fTokenLength = lexem.endoffset - fTokenOffset;
				lexem = null;
				return ACTION;
		}
		/* default, eoi */
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

	private static class SkippingLapgLexer extends LapgLexer {
		public SkippingLapgLexer(Reader stream) throws IOException {
			super(stream, new ErrorReporter() {
				public void error(int start, int end, int line, String s) {
				}
			});
		}

		@Override
		protected boolean createToken(LapgSymbol lapg_n) {
			switch (lapg_n.lexem) {
				case Lexems._skip_comment:
				case Lexems.scon:
				case Lexems.regexp:
				case Lexems.eoi:
					return true;
				case Lexems.LCURLY:
				case Lexems.RCURLY:
				case Lexems.iLCURLY:
					return super.createToken(lapg_n);
			}
			return false;
		}
	}
}
