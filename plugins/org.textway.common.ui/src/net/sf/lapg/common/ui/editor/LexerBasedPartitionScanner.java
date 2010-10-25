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
package net.sf.lapg.common.ui.editor;

import java.io.IOException;
import java.io.Reader;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;

public abstract class LexerBasedPartitionScanner implements IPartitionTokenScanner {

	private static class PartialDocumentReader extends Reader {
		private IDocument fDocument;
		private int currOffset;
		private int currEnd;

		public final void setRange(IDocument document, int offset, int length) {
			fDocument = document;
			currOffset = offset;
			currEnd = offset + length;
		}

		@Override
		public int read(char[] cbuf, int offset, int len) throws IOException {
			if (currOffset + len > currEnd) {
				len = currEnd - currOffset;
			}
			if(len == 0) {
				return 0;
			}
			try {
				final String content= fDocument.get(currOffset, len);
				currOffset += len;
				content.getChars(0, len, cbuf, offset);
			} catch (BadLocationException e) {
			}
			return len;
		}

		@Override
		public void close() throws IOException {
			fDocument = null;
		}
	}

	private final PartialDocumentReader fReader = new PartialDocumentReader();

	protected int fTokenOffset;
	protected int fTokenLength;

	public void setPartialRange(IDocument document, int offset, int length, String contentType, int partitionOffset) {
		if (partitionOffset > -1 && offset > partitionOffset) {
			// restart at the beginning of partition
			setRange(document, partitionOffset, length + offset - partitionOffset);
			return;
		}
		setRange(document, offset, length);
	}

	public void setRange(IDocument document, int offset, int length) {
		fReader.setRange(document, offset, length);
		reset(fReader, offset, length);
		fTokenOffset = offset;
		fTokenLength = 0;
	}

	public int getTokenLength() {
		return fTokenLength;
	}

	public int getTokenOffset() {
		return fTokenOffset;
	}

	protected abstract void reset(Reader reader, int offset, int length);
}
