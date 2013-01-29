/**
 * This file is part of Lapg.UI project.
 *
 * Copyright (c) 2010 Evgeny Gryaznov
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Gryaznov - initial API and implementation
 */
package org.textmapper.lapg.ui.editor;

import java.io.IOException;
import java.io.Reader;

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.textmapper.lapg.common.ui.editor.LexerBasedPartitionScanner;
import org.textmapper.tool.parser.TMLexer;
import org.textmapper.tool.parser.TMLexer.LapgSymbol;
import org.textmapper.tool.parser.TMLexer.Lexems;

public class LapgPartitionScanner extends LexerBasedPartitionScanner implements IPartitions {

	private final IToken fText = new Token(null);
	private final IToken fComment = new Token(LAPG_COMMENT_LINE);
	private final IToken fRegexp = new Token(LAPG_REGEXP);
	private final IToken fString = new Token(LAPG_STRING);
	private final IToken fAction = new Token(LAPG_ACTION);
	private final IToken fTemplates = new Token(LAPG_TEMPLATES);

	private TMLexer lexer;
	private LapgSymbol lexem;
	private int fDocumentLength;

	public LapgPartitionScanner() {
	}

	public IToken nextToken() {
		fTokenOffset += fTokenLength;
		if (lexem == null) {
			readNext();
		}
		if (fTokenOffset < lexem.offset) {
			fTokenLength = lexem.offset - fTokenOffset;
			return fText;
		}
		int token = lexem.symbol;
		switch (token) {
			case Lexems._skip_comment:
			case Lexems.scon:
			case Lexems.regexp:
				fTokenLength = lexem.endoffset - fTokenOffset;
				lexem = null;
				if (token == Lexems._skip_comment) {
					return fComment;
				} else if (token == Lexems.scon) {
					return fString;
				} else {
					return fRegexp;
				}
			case Lexems.code:
				fTokenLength = lexem.endoffset - fTokenOffset;
				lexem = null;
				return fAction;
		}
		/* default, eoi */
		if (lexem.endoffset < fDocumentLength) {
			fTokenLength = fDocumentLength - fTokenOffset;
			lexem.offset = lexem.endoffset = fDocumentLength;
			return fTemplates;
		}
		return Token.EOF;
	}

	private void readNext() {
		try {
			lexem = lexer.next();
		} catch (IOException e) {
			/* never happens */
		}
	}

	@Override
	protected void reset(Reader reader, int offset, int length) {
		fDocumentLength = offset + length;
		try {
			if (lexer == null) {
				lexer = new SkippingTMLexer(reader);
			} else {
				lexer.reset(reader);
			}
		} catch (IOException ex) {
			/* never happens */
		}
		lexer.setOffset(offset);
		lexem = null;
	}

	private static class SkippingTMLexer extends TMLexer {
		public SkippingTMLexer(Reader stream) throws IOException {
			super(stream, new ErrorReporter() {
				public void error(int start, int end, int line, String s) {
				}
			});
		}

		@Override
		protected boolean createToken(LapgSymbol lapg_n, int lexemIndex) throws IOException {
			switch (lapg_n.symbol) {
				case Lexems._skip_comment:
				case Lexems.scon:
				case Lexems.regexp:
				case Lexems.eoi:
					return true;
				case Lexems.code:
					return super.createToken(lapg_n, lexemIndex);
			}
			return false;
		}
	}
}
