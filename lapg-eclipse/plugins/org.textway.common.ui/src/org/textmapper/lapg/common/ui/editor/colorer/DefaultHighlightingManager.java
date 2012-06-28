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
package org.textmapper.lapg.common.ui.editor.colorer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

public class DefaultHighlightingManager {

	private static final String OPT_COLOR = ".color"; //$NON-NLS-1$
	private static final String OPT_BOLD = ".bold"; //$NON-NLS-1$
	private static final String OPT_ITALIC = ".italic"; //$NON-NLS-1$
	private static final String OPT_STRIKETHROUGH = ".strikethrough"; //$NON-NLS-1$
	private static final String OPT_UNDERLINE = ".underline"; //$NON-NLS-1$
	private static final String OPT_ENABLED = ".enabled"; //$NON-NLS-1$

	public class ColorDescriptor {

		private TextAttribute fAttribute;

		private Highlighting fHighlighting;
		private Token fToken;

		private final String fDisplayName;

		private final String fEnabledKey;
		private final String fBoldKey;
		private final String fItalicKey;
		private final String fColorKey;
		private final String fStrikethroughKey;
		private final String fUnderlineKey;

		public ColorDescriptor(String key, String displayName, boolean canBeDisabled) {
			String baseKey = key;
			fDisplayName = displayName;

			fBoldKey = baseKey + OPT_BOLD;
			fItalicKey = baseKey + OPT_ITALIC;
			fColorKey = baseKey + OPT_COLOR;
			fStrikethroughKey = baseKey + OPT_STRIKETHROUGH;
			fUnderlineKey = baseKey + OPT_UNDERLINE;
			fEnabledKey = canBeDisabled ? baseKey + OPT_ENABLED : null;
		}

		public boolean canBeDisabled() {
			return fEnabledKey != null;
		}

		public String getDisplayName() {
			return fDisplayName;
		}

		public String getEnabledKey() {
			return fEnabledKey;
		}

		public String getBoldKey() {
			return fBoldKey;
		}

		public String getItalicKey() {
			return fItalicKey;
		}

		public String getStrikethroughKey() {
			return fStrikethroughKey;
		}

		public String getUnderlineKey() {
			return fUnderlineKey;
		}

		public String getColorKey() {
			return fColorKey;
		}

		public Highlighting getHighlighting() {
			return fHighlighting;
		}

		public boolean isAffectedBy(String property) {
			return property.equals(getColorKey()) || property.equals(getBoldKey()) || property.equals(getEnabledKey())
			|| property.equals(getItalicKey()) || property.equals(getStrikethroughKey())
			|| property.equals(getUnderlineKey());
		}

		/**
		 * UI thread only
		 */
		public TextAttribute createTextAttribute() {
			if (fAttribute != null) {
				return fAttribute;
			}

			addColor(getColorKey());

			int style = fStore.getBoolean(getBoldKey()) ? SWT.BOLD : SWT.NORMAL;

			if (fStore.getBoolean(getItalicKey())) {
				style |= SWT.ITALIC;
			}

			if (fStore.getBoolean(getStrikethroughKey())) {
				style |= TextAttribute.STRIKETHROUGH;
			}

			if (fStore.getBoolean(getUnderlineKey())) {
				style |= TextAttribute.UNDERLINE;
			}

			fAttribute = new TextAttribute(fColorManager.getColor(PreferenceConverter.getColor(fStore, getColorKey())),
					null, style);
			return fAttribute;
		}

		/**
		 * UI thread only
		 */
		public Highlighting createHighlighting() {
			if (fHighlighting != null) {
				return fHighlighting;
			}

			boolean isEnabled = canBeDisabled() ? fStore.getBoolean(getEnabledKey()) : true;
			fHighlighting = new Highlighting(createTextAttribute(), isEnabled);
			return fHighlighting;
		}

		public Token createToken() {
			if (fToken != null) {
				return fToken;
			}
			fToken = new Token(createTextAttribute());
			return fToken;
		}

		public void disposeColor() {
			if (fAttribute != null) {
				removeColor(getColorKey());
				fAttribute = null;
			}
		}

		public TextAttribute getTextAttribute() {
			return fAttribute;
		}

		public void setTextAttribute(TextAttribute ta) {
			fAttribute = ta;
			if (fHighlighting != null) {
				fHighlighting.setTextAttribute(ta);
			}
			if (fToken != null) {
				fToken.setData(ta);
			}
		}
	}

	public class Highlighting {

		private TextAttribute fTextAttribute;
		private boolean fIsEnabled;

		public Highlighting(TextAttribute textAttribute, boolean isEnabled) {
			setTextAttribute(textAttribute);
			setEnabled(isEnabled);
		}

		public TextAttribute getTextAttribute() {
			return fTextAttribute;
		}

		public void setTextAttribute(TextAttribute textAttribute) {
			fTextAttribute = textAttribute;
		}

		public boolean isEnabled() {
			return fIsEnabled;
		}

		public void setEnabled(boolean isEnabled) {
			fIsEnabled = isEnabled;
		}
	}

	ListenerList fColorChangedListeners = new ListenerList();
	private final List<ColorDescriptor> fDescriptors;
	private final Map<String, ColorDescriptor> fKey2descriptor;

	private final IPreferenceStore fStore;
	private final DefaultColorManager fColorManager;
	private final String fPreferencesPrefix;
	private final List<ColorDefinition> fColors = new ArrayList<ColorDefinition>();

	public DefaultHighlightingManager(IPreferenceStore store, DefaultColorManager manager, String preferencesPrefix) {
		fPreferencesPrefix = preferencesPrefix;
		fStore = store;
		fColorManager = manager;
		initColors(fColors);
		fDescriptors = new ArrayList<ColorDescriptor>();
		fKey2descriptor = new HashMap<String, ColorDescriptor>();
		for(ColorDefinition def : fColors) {
			addDescriptor(def.getId(), def.getLabel(), def.isCanBeDisabled());
		}
	}

	public List<ColorDefinition> getColors() {
		return Collections.unmodifiableList(fColors);
	}

	public List<ColorGroupDefinition> getGroups() {
		List<ColorGroupDefinition> groups = new ArrayList<ColorGroupDefinition>();
		initGroups(groups);
		return Collections.unmodifiableList(groups);
	}

	public ColorDescriptor getColor(String id) {
		return fKey2descriptor.get(id);
	}

	private void addDescriptor(String key, String string, boolean canBeDisabled) {
		ColorDescriptor cd = new ColorDescriptor(fPreferencesPrefix + key, string, canBeDisabled);
		fKey2descriptor.put(key, cd);
		fDescriptors.add(cd);
	}

	protected void initColors(List<ColorDefinition> colors) {
		colors.add(new ColorDefinition(ICommonColors.COLOR_DEFAULT, "Others", "main", false, new RGB(0, 0, 0), false, false, false, false));
		colors.add(new ColorDefinition(ICommonColors.COLOR_IDENTIFIER, "Words", "main", false, new RGB(0, 0, 0), false, false, false, false));
		colors.add(new ColorDefinition(ICommonColors.COLOR_KEYWORD, "Keywords", "main", false, new RGB(127, 0, 85), true, false, false, false));
		colors.add(new ColorDefinition(ICommonColors.COLOR_STRING, "Strings", "main", false, new RGB(42, 0, 255), false, false, false, false));
		colors.add(new ColorDefinition(ICommonColors.COLOR_NUMBER, "Numbers", "main", false, new RGB(0, 0, 0), false, false, false, false));
		colors.add(new ColorDefinition(ICommonColors.COLOR_OPERATORS, "Operators", "main", false, new RGB(0, 0, 0), false, false, false, false));
		colors.add(new ColorDefinition(ICommonColors.COLOR_BRACKETS, "Brackets", "main", false, new RGB(0, 0, 0), false, false, false, false));

		colors.add(new ColorDefinition(ICommonColors.COLOR_COMMENT_MULTI, "Multi-line comment", "comment", false, new RGB(63, 127, 95), false, false, false, false));
		colors.add(new ColorDefinition(ICommonColors.COLOR_COMMENT_LINE, "Single-line comment", "comment", false, new RGB(63, 127, 95), false, false, false, false));
		colors.add(new ColorDefinition(ICommonColors.COLOR_TASK, "Task Tags", "comment", false, new RGB(127, 159, 191), true, false, false, false));
	}

	protected void initGroups(List<ColorGroupDefinition> groups) {
		groups.add(new ColorGroupDefinition("main", "Source"));
		groups.add(new ColorGroupDefinition("comment", "Comments"));
	}

	public void initializeDefaults() {
		for(ColorDefinition def : fColors) {
			init(def.getId(), def.getRgb(), def.isBold(), def.isItalic(), def.isUnderline(), def.isStrikethrough());
		}
	}

	private void init(String id, RGB rgb, boolean bold, boolean italic, boolean underline, boolean strikethrough) {
		ColorDescriptor cd = getColor(id);
		PreferenceConverter.setDefault(fStore, cd.getColorKey(), rgb);
		fStore.setDefault(cd.getBoldKey(), bold);
		fStore.setDefault(cd.getItalicKey(), italic);
		fStore.setDefault(cd.getUnderlineKey(), underline);
		fStore.setDefault(cd.getStrikethroughKey(), strikethrough);
		if (cd.canBeDisabled()) {
			fStore.setDefault(cd.getEnabledKey(), true);
		}
	}

	public void dispose() {
		for (ColorDescriptor cd : fDescriptors) {
			cd.disposeColor();
		}
	}

	public void propertyChange(PropertyChangeEvent event) {
		if (fStore == null) {
			return;
		}

		String property = event.getProperty();

		for (ColorDescriptor cd : fDescriptors) {
			if (cd.getColorKey().equals(property)) {
				adaptToTextForegroundChange(cd, event);
				notifyStyleChanged(cd);
				break;
			}

			if (cd.getBoldKey().equals(property)) {
				adaptToTextStyleChange(cd, event, SWT.BOLD);
				notifyStyleChanged(cd);
				break;
			}

			if (cd.getItalicKey().equals(property)) {
				adaptToTextStyleChange(cd, event, SWT.ITALIC);
				notifyStyleChanged(cd);
				break;
			}

			if (cd.getStrikethroughKey().equals(property)) {
				adaptToTextStyleChange(cd, event, TextAttribute.STRIKETHROUGH);
				notifyStyleChanged(cd);
				break;
			}

			if (cd.getUnderlineKey().equals(property)) {
				adaptToTextStyleChange(cd, event, TextAttribute.UNDERLINE);
				notifyStyleChanged(cd);
				break;
			}

			if (cd.canBeDisabled() && cd.getEnabledKey().equals(property)) {
				adaptToEnablementChange(cd.getHighlighting(), event);
				notifyStyleChanged(cd);
				break;
			}
		}
	}

	public void addHighlightingChangedListener(IHighlightingStyleListener listener) {
		fColorChangedListeners.add(listener);
	}

	public void removeHighlightingChangedListener(IHighlightingStyleListener listener) {
		fColorChangedListeners.remove(listener);
	}

	private void notifyStyleChanged(ColorDescriptor cd) {
		Object[] listeners = fColorChangedListeners.getListeners();
		for (int i = 0; i < listeners.length; ++i) {
			((IHighlightingStyleListener) listeners[i]).highlightingStyleChanged(cd);
		}
	}

	private void adaptToEnablementChange(Highlighting highlighting, PropertyChangeEvent event) {
		if (highlighting == null) {
			return;
		}
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

	private void adaptToTextForegroundChange(ColorDescriptor descriptor, PropertyChangeEvent event) {
		if (descriptor.getTextAttribute() == null) {
			return;
		}
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

			if ((color == null || !rgb.equals(color.getRGB()))) {
				fColorManager.unbindColor(property);
				fColorManager.bindColor(property, rgb);
				color = fColorManager.getColor(property);
			}

			TextAttribute oldAttr = descriptor.getTextAttribute();
			descriptor.setTextAttribute(new TextAttribute(color, oldAttr.getBackground(), oldAttr.getStyle()));
		}
	}

	private void adaptToTextStyleChange(ColorDescriptor descriptor, PropertyChangeEvent event, int styleAttribute) {
		if (descriptor.getTextAttribute() == null) {
			return;
		}
		boolean eventValue = false;
		Object value = event.getNewValue();
		if (value instanceof Boolean) {
			eventValue = ((Boolean) value).booleanValue();
		} else if (IPreferenceStore.TRUE.equals(value)) {
			eventValue = true;
		}

		TextAttribute oldAttr = descriptor.getTextAttribute();
		boolean activeValue = (oldAttr.getStyle() & styleAttribute) == styleAttribute;

		if (activeValue != eventValue) {
			descriptor.setTextAttribute(new TextAttribute(oldAttr.getForeground(), oldAttr.getBackground(),
					eventValue ? oldAttr.getStyle() | styleAttribute : oldAttr.getStyle() & ~styleAttribute));
		}
	}

	private void addColor(String colorKey) {
		if (fColorManager != null && colorKey != null && fColorManager.getColor(colorKey) == null) {
			RGB rgb = PreferenceConverter.getColor(fStore, colorKey);
			fColorManager.unbindColor(colorKey);
			fColorManager.bindColor(colorKey, rgb);
		}
	}

	private void removeColor(String colorKey) {
		fColorManager.unbindColor(colorKey);
	}

	public boolean isAffected(PropertyChangeEvent event) {
		String property = event.getProperty();
		for (ColorDescriptor cd : fDescriptors) {
			if (cd.isAffectedBy(property)) {
				return true;
			}
		}
		return false;
	}

	public ColorDescriptor[] getSemanticHighlightings() {
		return new ColorDescriptor[0];
	}
}
