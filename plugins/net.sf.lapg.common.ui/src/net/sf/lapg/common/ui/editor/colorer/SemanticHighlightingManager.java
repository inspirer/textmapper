package net.sf.lapg.common.ui.editor.colorer;

import net.sf.lapg.common.ui.editor.StructuredTextEditor;
import net.sf.lapg.common.ui.editor.StructuredTextViewer;
import net.sf.lapg.common.ui.editor.StructuredTextViewerConfiguration;
import net.sf.lapg.common.ui.editor.StructuredTextViewerConfiguration.StructuredTextPresentationReconciler;
import net.sf.lapg.common.ui.editor.colorer.DefaultHighlightingManager.ColorDescriptor;
import net.sf.lapg.common.ui.editor.colorer.DefaultHighlightingManager.Highlighting;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;

public class SemanticHighlightingManager implements IPropertyChangeListener, IHighlightingStyleListener {

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
	private ColorDescriptor[] fSemanticHighlightings;

	/** The editor */
	private StructuredTextEditor fEditor;
	/** The source viewer */
	private StructuredTextViewer fSourceViewer;
	/** The color manager */
	private DefaultHighlightingManager fHighlightingManager;
	/** The preference store */
	private IPreferenceStore fPreferenceStore;
	/** The source viewer configuration */
	private StructuredTextViewerConfiguration fConfiguration;
	/** The presentation reconciler */
	private StructuredTextPresentationReconciler fPresentationReconciler;

	/**
	 * Install the semantic highlighting on the given editor infrastructure
	 */
	public void install(StructuredTextEditor editor, StructuredTextViewer sourceViewer,
			DefaultHighlightingManager highlightingManager, IPreferenceStore preferenceStore) {
		fEditor = editor;
		fSourceViewer = sourceViewer;
		fHighlightingManager = highlightingManager;
		fPreferenceStore = preferenceStore;
		if (fEditor != null) {
			fConfiguration = editor.createSourceViewerConfiguration();
			fPresentationReconciler = (StructuredTextPresentationReconciler) fConfiguration
					.getPresentationReconciler(sourceViewer);
		} else {
			fConfiguration = null;
			fPresentationReconciler = null;
		}

		fPreferenceStore.addPropertyChangeListener(this);
		highlightingManager.addHighlightingChangedListener(this);

		if (isSemanticHighlightingEnabled()) {
			enable();
		}
	}

	/**
	 * Install the semantic highlighting on the given source viewer
	 * infrastructure. No reconciliation will be performed.
	 */
	public void install(StructuredTextViewer sourceViewer, DefaultHighlightingManager colorManager,
			IPreferenceStore preferenceStore) {
		install(null, sourceViewer, colorManager, preferenceStore);
	}

	/**
	 * Enable semantic highlighting.
	 */
	private void enable() {
		for (ColorDescriptor fSemanticHighlighting : fSemanticHighlightings) {
			fSemanticHighlighting.createHighlighting();
		}

		fPresenter = new SemanticHighlightingPresenter();
		fPresenter.install(fSourceViewer, fPresentationReconciler);

		if (fEditor != null) {
			fReconciler = new SemanticHighlightingReconciler();
			fReconciler.install(fEditor, fSourceViewer, fPresenter, fSemanticHighlightings);
		} else {
			// fPresenter.updatePresentation(null, createHardcodedPositions(), new HighlightedPosition[0]);
		}
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
		if (fHighlightingManager != null) {
			fHighlightingManager.removeHighlightingChangedListener(this);
		}

		fEditor = null;
		fSourceViewer = null;
		fHighlightingManager = null;
		fConfiguration = null;
		fPresentationReconciler = null;
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
	}

	private boolean isSemanticHighlightingEnabled() {
		for (ColorDescriptor fSemanticHighlighting : fSemanticHighlightings) {
			String enabledKey = fSemanticHighlighting.getEnabledKey();
			if (fPreferenceStore.getBoolean(enabledKey)) {
				return true;
			}
		}

		return false;

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

		if (affectsEnablement(fPreferenceStore, event)) {
			if (isSemanticHighlightingEnabled()) {
				enable();
				if (fReconciler != null) {
					fReconciler.refresh();
				}
			} else {
				disable();
			}
		}
	}

	public boolean affectsEnablement(IPreferenceStore store, PropertyChangeEvent event) {
		String relevantKey = null;
		ColorDescriptor[] highlightings = fSemanticHighlightings;
		for (ColorDescriptor highlighting : highlightings) {
			if (event.getProperty().equals(highlighting.getEnabledKey())) {
				relevantKey = event.getProperty();
				break;
			}
		}
		if (relevantKey == null) {
			return false;
		}

		for (ColorDescriptor highlighting : highlightings) {
			String key = highlighting.getEnabledKey();
			if (key.equals(relevantKey)) {
				continue;
			}
			if (store.getBoolean(key)) {
				return false; // another is still enabled or was enabled before
			}
		}

		// all others are disabled, so toggling relevantKey affects the
		// enablement
		return true;
	}

	public void highlightingStyleChanged(ColorDescriptor cd) {
		if (fPresenter != null && cd.getHighlighting() != null) {
			fPresenter.highlightingStyleChanged(cd.getHighlighting());
		}
		if (fReconciler != null) {
			fReconciler.refresh();
		}
	}

	public SemanticHighlightingReconciler getReconciler() {
		return fReconciler;
	}
}
