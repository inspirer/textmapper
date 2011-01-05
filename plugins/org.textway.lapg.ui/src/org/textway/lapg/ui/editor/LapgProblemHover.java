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
package org.textway.lapg.ui.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Point;

public class LapgProblemHover implements IAnnotationHover, ITextHover {

	private final ISourceViewer sourceViewer;

	public LapgProblemHover(final ISourceViewer sourceViewer) {
		this.sourceViewer = sourceViewer;
	}

	public String getHoverInfo(final ISourceViewer sourceViewer, final int lineNumber) {
		return getHoverInfoInternal(lineNumber, -1);
	}

	public String getHoverInfo(final ITextViewer textViewer, final IRegion hoverRegion) {
		int lineNumber;
		try {
			lineNumber = sourceViewer.getDocument().getLineOfOffset(hoverRegion.getOffset());
		} catch (final BadLocationException e) {
			return null;
		}
		return getHoverInfoInternal(lineNumber, hoverRegion.getOffset());
	}

	public IRegion getHoverRegion(final ITextViewer textViewer, final int offset) {
		final Point selection = textViewer.getSelectedRange();
		if (selection.x <= offset && offset < selection.x + selection.y) {
			return new Region(selection.x, selection.y);
		}
		return new Region(offset, 0);
	}

	@SuppressWarnings("unchecked")
	private String getHoverInfoInternal(final int lineNumber, final int offset) {
		final IAnnotationModel model = sourceViewer.getAnnotationModel();
		final List<String> messages = new ArrayList<String>();

		final Iterator<Annotation> iterator = model.getAnnotationIterator();
		while (iterator.hasNext()) {
			final Annotation annotation = iterator.next();
			if (!(annotation instanceof Annotation && annotation.getType().startsWith(LapgReconcilingStrategy.ANNOTATION_PREFIX))) {
				continue;
			}
			final int start = model.getPosition(annotation).getOffset();
			final int end = start + model.getPosition(annotation).getLength();

			if (offset > 0 && !(start <= offset && offset <= end)) {
				continue;
			}
			try {
				if (lineNumber != sourceViewer.getDocument().getLineOfOffset(start)) {
					continue;
				}
			} catch (final Exception x) {
				continue;
			}
			messages.add(annotation.getText().trim());
		}
		return formatInfo(messages);
	}

	private StringBuffer buffer;

	private String formatInfo(final List<String> messages) {
		buffer = new StringBuffer();
		if (messages.size() > 1) {
			buffer.append("Multiple markers at this line\n");
			final Iterator<String> e = messages.iterator();
			while (e.hasNext()) {
				splitInfo("- " + e.next() + "\n");
			}
		} else if (messages.size() == 1) {
			splitInfo(messages.get(0));
		}
		return buffer.toString();
	}

	private String splitInfo(String message) {
		String prefix = "";
		int pos;
		do {
			pos = message.indexOf(" ", 60);
			if (pos > -1) {
				buffer.append(prefix + message.substring(0, pos) + "\n");
				message = message.substring(pos);
				prefix = "  ";
			} else {
				buffer.append(prefix + message);
			}
		} while (pos > -1);
		return buffer.toString();
	}
}
