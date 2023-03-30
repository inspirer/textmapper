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
import org.textmapper.tool.parser.TMLexer.Span;
import org.textmapper.tool.parser.TMLexer.Tokens;

public class LapgPartitionScanner extends LexerBasedPartitionScanner implements IPartitions {

	private final IToken fText = new Token(null);
	private final IToken fComment = new Token(LAPG_COMMENT_LINE);
	private final IToken fRegexp = new Token(LAPG_REGEXP);
	private final IToken fString = new Token(LAPG_STRING);
	private final IToken fAction = new Token(LAPG_ACTION);
	private final IToken fTemplates = new Token(LAPG_TEMPLATES);

	private TMLexer lexer;
	private Span token;
	private int fDocumentLength;

	public LapgPartitionScanner() {
	}

	public IToken nextToken() {
		fTokenOffset += fTokenLength;
		if (token == null) {
			readNext();
		}
		if (fTokenOffset < token.offset) {
			fTokenLength = token.offset - fTokenOffset;
			return fText;
		}
		int symbol = token.symbol;
		switch (symbol) {
			case Tokens._skip_comment:
			case Tokens.scon:
			case Tokens.regexp:
				fTokenLength = token.endoffset - fTokenOffset;
				token = null;
				if (symbol == Tokens._skip_comment) {
					return fComment;
				} else if (symbol == Tokens.scon) {
					return fString;
				} else {
					return fRegexp;
				}
			case Tokens.code:
				fTokenLength = token.endoffset - fTokenOffset;
				token = null;
				return fAction;
		}
		/* default, eoi */
		if (token.endoffset < fDocumentLength) {
			fTokenLength = fDocumentLength - fTokenOffset;
			token.offset = token.endoffset = fDocumentLength;
			return fTemplates;
		}
		return Token.EOF;
	}

	private void readNext() {
		try {
			token = lexer.next();
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
		token = null;
	}

	private static class SkippingTMLexer extends TMLexer {
		public SkippingTMLexer(Reader stream) throws IOException {
			super(stream, new ErrorReporter() {
				public void error(String message, int line, int offset, int endoffset) {
				}
			});
		}

		@Override
		protected boolean createToken(Span token, int ruleIndex) throws IOException {
			switch (token.symbol) {
				case Tokens._skip_comment:
				case Tokens.scon:
				case Tokens.regexp:
				case Tokens.eoi:
					return true;
				case Tokens.code:
					return super.createToken(token, ruleIndex);
			}
			return false;
		}
	}
}
