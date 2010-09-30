package net.sf.lapg.common.ui.editor.colorer;

import java.util.ArrayList;
import java.util.List;

import net.sf.lapg.common.ui.editor.colorer.DefaultHighlightingManager.Highlighting;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public class SemanticHighlightingManager implements IPropertyChangeListener {

	static class HighlightedPosition extends Position {

		/** Highlighting of the position */
		private final Highlighting fStyle;

		/** Lock object */
		private final Object fLock;

		/**
		 * Initialize the styled positions with the given offset, length and
		 * foreground color.
		 */
		public HighlightedPosition(int offset, int length, Highlighting highlighting, Object lock) {
			super(offset, length);
			fStyle = highlighting;
			fLock = lock;
		}

		/**
		 * @return Returns a corresponding style range.
		 */
		public StyleRange createStyleRange() {
			int len = 0;
			if (fStyle.isEnabled()) {
				len = getLength();
			}

			TextAttribute textAttribute = fStyle.getTextAttribute();
			int style = textAttribute.getStyle();
			int fontStyle = style & (SWT.ITALIC | SWT.BOLD | SWT.NORMAL);
			StyleRange styleRange = new StyleRange(getOffset(), len, textAttribute.getForeground(), textAttribute
					.getBackground(), fontStyle);
			styleRange.strikeout = (style & TextAttribute.STRIKETHROUGH) != 0;
			styleRange.underline = (style & TextAttribute.UNDERLINE) != 0;

			return styleRange;
		}

		/**
		 * Uses reference equality for the highlighting.
		 */
		public boolean isEqual(int off, int len, Highlighting highlighting) {
			synchronized (fLock) {
				return !isDeleted() && getOffset() == off && getLength() == len && fStyle == highlighting;
			}
		}

		/**
		 * Is this position contained in the given range (inclusive)?
		 * Synchronizes on position updater.
		 */
		public boolean isContained(int off, int len) {
			synchronized (fLock) {
				return !isDeleted() && off <= getOffset() && off + len >= getOffset() + getLength();
			}
		}

		public void update(int off, int len) {
			synchronized (fLock) {
				super.setOffset(off);
				super.setLength(len);
			}
		}

		@Override
		public void setLength(int length) {
			synchronized (fLock) {
				super.setLength(length);
			}
		}

		@Override
		public void setOffset(int offset) {
			synchronized (fLock) {
				super.setOffset(offset);
			}
		}

		@Override
		public void delete() {
			synchronized (fLock) {
				super.delete();
			}
		}

		@Override
		public void undelete() {
			synchronized (fLock) {
				super.undelete();
			}
		}

		/**
		 * @return Returns the highlighting.
		 */
		public Highlighting getHighlighting() {
			return fStyle;
		}
	}

	/**
	 * Highlighted ranges.
	 */
	public static class HighlightedRange extends Region {
		/**
		 * The highlighting key as returned by
		 * {@link SemanticHighlighting#getPreferenceKey()}.
		 */
		private final String fKey;

		/**
		 * Initialize with the given offset, length and highlighting key.
		 */
		public HighlightedRange(int offset, int length, String key) {
			super(offset, length);
			fKey = key;
		}

		/**
		 * @return the highlighting key as returned by
		 *         {@link SemanticHighlighting#getPreferenceKey()}
		 */
		public String getKey() {
			return fKey;
		}

		@Override
		public boolean equals(Object o) {
			return super.equals(o) && o instanceof HighlightedRange && fKey.equals(((HighlightedRange) o).getKey());
		}

		@Override
		public int hashCode() {
			return super.hashCode() | fKey.hashCode();
		}
	}

	/** Semantic highlighting presenter */
	private SemanticHighlightingPresenter fPresenter;
	/** Semantic highlighting reconciler */
	private SemanticHighlightingReconciler fReconciler;

	/** Semantic highlightings */
	private SemanticHighlighting[] fSemanticHighlightings;
	/** Highlightings */
	private Highlighting[] fHighlightings;

	/** The editor */
	private StructuredTextEditor fEditor;
	/** The source viewer */
	private StructuredTextSourceViewer fSourceViewer;
	/** The color manager */
	private IColorManager fColorManager;
	/** The preference store */
	private IPreferenceStore fPreferenceStore;
	/** The source viewer configuration */
	private StructuredTextSourceViewerConfiguration fConfiguration;
	/** The presentation reconciler */
	private StructuredTextPresentationReconciler fPresentationReconciler;

	/** The hard-coded ranges */
	private HighlightedRange[][] fHardcodedRanges;

	/**
	 * Install the semantic highlighting on the given editor infrastructure
	 */
	public void install(StructuredTextEditor editor, StructuredTextSourceViewer sourceViewer,
			IColorManager colorManager, IPreferenceStore preferenceStore) {
		fEditor = editor;
		fSourceViewer = sourceViewer;
		fColorManager = colorManager;
		fPreferenceStore = preferenceStore;
		if (fEditor != null) {
			fConfiguration = editor.createStructuredTextSourceViewerConfiguration();
			fPresentationReconciler = (StructuredTextPresentationReconciler) fConfiguration
					.getPresentationReconciler(sourceViewer);
		} else {
			fConfiguration = null;
			fPresentationReconciler = null;
		}

		fPreferenceStore.addPropertyChangeListener(this);

		if (isEnabled()) {
			enable();
		}
	}

	/**
	 * Install the semantic highlighting on the given source viewer
	 * infrastructure. No reconciliation will be performed.
	 */
	public void install(StructuredTextSourceViewer sourceViewer, DefaultHighlightingManager colorManager,
			IPreferenceStore preferenceStore, HighlightedRange[][] hardcodedRanges) {
		fHardcodedRanges = hardcodedRanges;
		install(null, sourceViewer, colorManager, preferenceStore);
	}

	/**
	 * Enable semantic highlighting.
	 */
	private void enable() {
		initializeHighlightings();

		fPresenter = new SemanticHighlightingPresenter();
		fPresenter.install(fSourceViewer, fPresentationReconciler);

		if (fEditor != null) {
			fReconciler = new SemanticHighlightingReconciler();
			fReconciler.install(fEditor, fSourceViewer, fPresenter, fSemanticHighlightings, fHighlightings);
		} else {
			fPresenter.updatePresentation(null, createHardcodedPositions(), new HighlightedPosition[0]);
		}
	}

	/**
	 * Computes the hard-coded positions from the hard-coded ranges
	 *
	 * @return the hard-coded positions
	 */
	private HighlightedPosition[] createHardcodedPositions() {
		List positions = new ArrayList();
		for (HighlightedRange[] fHardcodedRange : fHardcodedRanges) {
			HighlightedRange range = null;
			Highlighting hl = null;
			for (HighlightedRange element : fHardcodedRange) {
				hl = getHighlighting(element.getKey());
				if (hl.isEnabled()) {
					range = element;
					break;
				}
			}

			if (range != null) {
				positions.add(fPresenter.createHighlightedPosition(range.getOffset(), range.getLength(), hl));
			}
		}
		return (HighlightedPosition[]) positions.toArray(new HighlightedPosition[positions.size()]);
	}

	/**
	 * Returns the highlighting corresponding to the given key.
	 *
	 * @param key
	 *            the highlighting key as returned by
	 *            {@link SemanticHighlighting#getPreferenceKey()}
	 * @return the corresponding highlighting
	 */
	private Highlighting getHighlighting(String key) {
		for (int i = 0; i < fSemanticHighlightings.length; i++) {
			SemanticHighlighting semanticHighlighting = fSemanticHighlightings[i];
			if (key.equals(semanticHighlighting.getPreferenceKey())) {
				return fHighlightings[i];
			}
		}
		return null;
	}

	/**
	 * Uninstall the semantic highlighting
	 */
	public void uninstall() {
		disable();

		if (fPreferenceStore != null) {
			fPreferenceStore.removePropertyChangeListener(this);
			fPreferenceStore = null;
		}

		fEditor = null;
		fSourceViewer = null;
		fColorManager = null;
		fConfiguration = null;
		fPresentationReconciler = null;
		fHardcodedRanges = null;
	}

	/**
	 * Disable semantic highlighting.
	 */
	private void disable() {
		if (fReconciler != null) {
			fReconciler.uninstall();
			fReconciler = null;
		}

		if (fPresenter != null) {
			fPresenter.uninstall();
			fPresenter = null;
		}

		if (fSemanticHighlightings != null) {
			disposeHighlightings();
		}
	}

	/**
	 * @return <code>true</code> iff semantic highlighting is enabled in the
	 *         preferences
	 */
	private boolean isEnabled() {
		return SemanticHighlightings.isEnabled(fPreferenceStore);
	}

	/**
	 * Initialize semantic highlightings.
	 */
	private void initializeHighlightings() {
		fSemanticHighlightings = SemanticHighlightings.getSemanticHighlightings();
		fHighlightings = new Highlighting[fSemanticHighlightings.length];

		for (int i = 0, n = fSemanticHighlightings.length; i < n; i++) {
			SemanticHighlighting semanticHighlighting = fSemanticHighlightings[i];
			String colorKey = SemanticHighlightings.getColorPreferenceKey(semanticHighlighting);
			addColor(colorKey);

			String boldKey = SemanticHighlightings.getBoldPreferenceKey(semanticHighlighting);
			int style = fPreferenceStore.getBoolean(boldKey) ? SWT.BOLD : SWT.NORMAL;

			String italicKey = SemanticHighlightings.getItalicPreferenceKey(semanticHighlighting);
			if (fPreferenceStore.getBoolean(italicKey)) {
				style |= SWT.ITALIC;
			}

			String strikethroughKey = SemanticHighlightings.getStrikethroughPreferenceKey(semanticHighlighting);
			if (fPreferenceStore.getBoolean(strikethroughKey)) {
				style |= TextAttribute.STRIKETHROUGH;
			}

			String underlineKey = SemanticHighlightings.getUnderlinePreferenceKey(semanticHighlighting);
			if (fPreferenceStore.getBoolean(underlineKey)) {
				style |= TextAttribute.UNDERLINE;
			}

			boolean isEnabled = fPreferenceStore.getBoolean(SemanticHighlightings
					.getEnabledPreferenceKey(semanticHighlighting));

			fHighlightings[i] = new Highlighting(new TextAttribute(fColorManager.getColor(PreferenceConverter.getColor(
					fPreferenceStore, colorKey)), null, style), isEnabled);
		}
	}

	/**
	 * Dispose the semantic highlightings.
	 */
	private void disposeHighlightings() {
		for (int i = 0, n = fSemanticHighlightings.length; i < n; i++) {
			removeColor(SemanticHighlightings.getColorPreferenceKey(fSemanticHighlightings[i]));
		}

		fSemanticHighlightings = null;
		fHighlightings = null;
	}

	public void propertyChange(PropertyChangeEvent event) {
		handlePropertyChangeEvent(event);
	}

	/**
	 * Handle the given property change event
	 */
	private void handlePropertyChangeEvent(PropertyChangeEvent event) {
		if (fPreferenceStore == null) {
			return; // Uninstalled during event notification
		}

		if (fConfiguration != null) {
			fConfiguration.handlePropertyChangeEvent(event);
		}

		if (SemanticHighlightings.affectsEnablement(fPreferenceStore, event)) {
			if (isEnabled()) {
				enable();
			} else {
				disable();
			}
		}

		if (!isEnabled()) {
			return;
		}

		boolean refreshNeeded = false;

		for (int i = 0, n = fSemanticHighlightings.length; i < n; i++) {
			SemanticHighlighting semanticHighlighting = fSemanticHighlightings[i];

			String colorKey = SemanticHighlightings.getColorPreferenceKey(semanticHighlighting);
			if (colorKey.equals(event.getProperty())) {
				adaptToTextForegroundChange(fHighlightings[i], event);
				fPresenter.highlightingStyleChanged(fHighlightings[i]);
				refreshNeeded = true;
				continue;
			}

			String boldKey = SemanticHighlightings.getBoldPreferenceKey(semanticHighlighting);
			if (boldKey.equals(event.getProperty())) {
				adaptToTextStyleChange(fHighlightings[i], event, SWT.BOLD);
				fPresenter.highlightingStyleChanged(fHighlightings[i]);
				refreshNeeded = true;
				continue;
			}

			String italicKey = SemanticHighlightings.getItalicPreferenceKey(semanticHighlighting);
			if (italicKey.equals(event.getProperty())) {
				adaptToTextStyleChange(fHighlightings[i], event, SWT.ITALIC);
				fPresenter.highlightingStyleChanged(fHighlightings[i]);
				refreshNeeded = true;
				continue;
			}

			String strikethroughKey = SemanticHighlightings.getStrikethroughPreferenceKey(semanticHighlighting);
			if (strikethroughKey.equals(event.getProperty())) {
				adaptToTextStyleChange(fHighlightings[i], event, TextAttribute.STRIKETHROUGH);
				fPresenter.highlightingStyleChanged(fHighlightings[i]);
				refreshNeeded = true;
				continue;
			}

			String underlineKey = SemanticHighlightings.getUnderlinePreferenceKey(semanticHighlighting);
			if (underlineKey.equals(event.getProperty())) {
				adaptToTextStyleChange(fHighlightings[i], event, TextAttribute.UNDERLINE);
				fPresenter.highlightingStyleChanged(fHighlightings[i]);
				refreshNeeded = true;
				continue;
			}

			String enabledKey = SemanticHighlightings.getEnabledPreferenceKey(semanticHighlighting);
			if (enabledKey.equals(event.getProperty())) {
				adaptToEnablementChange(fHighlightings[i], event);
				fPresenter.highlightingStyleChanged(fHighlightings[i]);
				refreshNeeded = true;
				continue;
			}
		}

		if (refreshNeeded && fReconciler != null) {
			fReconciler.refresh();
		}
	}

	private void adaptToEnablementChange(Highlighting highlighting, PropertyChangeEvent event) {
		Object value = event.getNewValue();
		boolean eventValue;
		if (value instanceof Boolean) {
			eventValue = ((Boolean) value).booleanValue();
		} else if (IPreferenceStore.TRUE.equals(value)) {
			eventValue = true;
		} else {
			eventValue = false;
		}
		highlighting.setEnabled(eventValue);
	}

	private void adaptToTextForegroundChange(Highlighting highlighting, PropertyChangeEvent event) {
		RGB rgb = null;

		Object value = event.getNewValue();
		if (value instanceof RGB) {
			rgb = (RGB) value;
		} else if (value instanceof String) {
			rgb = StringConverter.asRGB((String) value);
		}

		if (rgb != null) {

			String property = event.getProperty();
			Color color = fColorManager.getColor(property);

			if ((color == null || !rgb.equals(color.getRGB())) && fColorManager instanceof IColorManagerExtension) {
				IColorManagerExtension ext = (IColorManagerExtension) fColorManager;
				ext.unbindColor(property);
				ext.bindColor(property, rgb);
				color = fColorManager.getColor(property);
			}

			TextAttribute oldAttr = highlighting.getTextAttribute();
			highlighting.setTextAttribute(new TextAttribute(color, oldAttr.getBackground(), oldAttr.getStyle()));
		}
	}

	private void adaptToTextStyleChange(Highlighting highlighting, PropertyChangeEvent event, int styleAttribute) {
		boolean eventValue = false;
		Object value = event.getNewValue();
		if (value instanceof Boolean) {
			eventValue = ((Boolean) value).booleanValue();
		} else if (IPreferenceStore.TRUE.equals(value)) {
			eventValue = true;
		}

		TextAttribute oldAttr = highlighting.getTextAttribute();
		boolean activeValue = (oldAttr.getStyle() & styleAttribute) == styleAttribute;

		if (activeValue != eventValue) {
			highlighting.setTextAttribute(new TextAttribute(oldAttr.getForeground(), oldAttr.getBackground(),
					eventValue ? oldAttr.getStyle() | styleAttribute : oldAttr.getStyle() & ~styleAttribute));
		}
	}

	private void addColor(String colorKey) {
		if (fColorManager != null && colorKey != null && fColorManager.getColor(colorKey) == null) {
			RGB rgb = PreferenceConverter.getColor(fPreferenceStore, colorKey);
			if (fColorManager instanceof IColorManagerExtension) {
				IColorManagerExtension ext = (IColorManagerExtension) fColorManager;
				ext.unbindColor(colorKey);
				ext.bindColor(colorKey, rgb);
			}
		}
	}

	private void removeColor(String colorKey) {
		if (fColorManager instanceof IColorManagerExtension) {
			((IColorManagerExtension) fColorManager).unbindColor(colorKey);
		}
	}

	public SemanticHighlightingReconciler getReconciler() {
		return fReconciler;
	}
}
