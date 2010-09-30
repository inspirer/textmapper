package net.sf.lapg.common.ui.editor.colorer;

import java.util.ArrayList;
import java.util.List;

import net.sf.lapg.common.ui.editor.colorer.DefaultHighlightingManager.ColorDescriptor;
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
	private ColorDescriptor[] fSemanticHighlightings;

	/** The editor */
	private StructuredTextEditor fEditor;
	/** The source viewer */
	private StructuredTextSourceViewer fSourceViewer;
	/** The color manager */
	private DefaultHighlightingManager fHighlightingManager;
	/** The preference store */
	private IPreferenceStore fPreferenceStore;
	/** The source viewer configuration */
	private StructuredTextSourceViewerConfiguration fConfiguration;
	/** The presentation reconciler */
	private StructuredTextPresentationReconciler fPresentationReconciler;

	/**
	 * Install the semantic highlighting on the given editor infrastructure
	 */
	public void install(StructuredTextEditor editor, StructuredTextSourceViewer sourceViewer,
			DefaultHighlightingManager highlightingManager, IPreferenceStore preferenceStore) {
		fEditor = editor;
		fSourceViewer = sourceViewer;
		fHighlightingManager = highlightingManager;
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
			fPresenter.updatePresentation(null, createHardcodedPositions(), new HighlightedPosition[0]);
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
			Color color = fHighlightingManager.getColor(property);

			if ((color == null || !rgb.equals(color.getRGB())) && fHighlightingManager instanceof IColorManagerExtension) {
				IColorManagerExtension ext = (IColorManagerExtension) fHighlightingManager;
				ext.unbindColor(property);
				ext.bindColor(property, rgb);
				color = fHighlightingManager.getColor(property);
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
		if (fHighlightingManager != null && colorKey != null && fHighlightingManager.getColor(colorKey) == null) {
			RGB rgb = PreferenceConverter.getColor(fPreferenceStore, colorKey);
			if (fHighlightingManager instanceof IColorManagerExtension) {
				IColorManagerExtension ext = (IColorManagerExtension) fHighlightingManager;
				ext.unbindColor(colorKey);
				ext.bindColor(colorKey, rgb);
			}
		}
	}

	private void removeColor(String colorKey) {
		if (fHighlightingManager instanceof IColorManagerExtension) {
			((IColorManagerExtension) fHighlightingManager).unbindColor(colorKey);
		}
	}

	public SemanticHighlightingReconciler getReconciler() {
		return fReconciler;
	}
}
