package org.textway.lapg.common.ui.editor;

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

public abstract class StructuredTextProblemHover implements IAnnotationHover, ITextHover {

	private final ISourceViewer sourceViewer;

	public StructuredTextProblemHover(final ISourceViewer sourceViewer) {
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

	protected abstract boolean canShow(Annotation annotation);

	@SuppressWarnings("unchecked")
	protected List<String> getHoverInfo(ISourceViewer viewer, int line, int offset) {
		IAnnotationModel model = viewer.getAnnotationModel();
		List<String> messages = new ArrayList<String>();

		final Iterator<Annotation> iterator = model.getAnnotationIterator();
		while (iterator.hasNext()) {
			final Annotation annotation = iterator.next();
			if (!canShow(annotation)) {
				continue;
			}
			final int start = model.getPosition(annotation).getOffset();
			final int end = start + model.getPosition(annotation).getLength();

			if (offset > 0 && !(start <= offset && offset <= end)) {
				continue;
			}
			try {
				if (line != viewer.getDocument().getLineOfOffset(start)) {
					continue;
				}
			} catch (Exception x) {
				continue;
			}
			messages.add(annotation.getText().trim());
		}
		return messages;
	}

	private String getHoverInfoInternal(int lineNumber, int offset) {
		return formatInfo(getHoverInfo(sourceViewer, lineNumber, offset));
	}

	private static String formatInfo(final List<String> messages) {
		StringBuilder sb = new StringBuilder();
		if (messages.size() > 1) {
			sb.append("Multiple markers at this line\n");
			final Iterator<String> e = messages.iterator();
			while (e.hasNext()) {
				splitInfo("- " + e.next() + "\n", sb);
			}
		} else if (messages.size() == 1) {
			splitInfo(messages.get(0), sb);
		}
		return sb.toString();
	}

	private static String splitInfo(String message, StringBuilder sb) {
		String prefix = "";
		int pos;
		do {
			pos = message.indexOf(" ", 60);
			if (pos > -1) {
				sb.append(prefix + message.substring(0, pos) + "\n");
				message = message.substring(pos);
				prefix = "  ";
			} else {
				sb.append(prefix + message);
			}
		} while (pos > -1);
		return sb.toString();
	}
}
