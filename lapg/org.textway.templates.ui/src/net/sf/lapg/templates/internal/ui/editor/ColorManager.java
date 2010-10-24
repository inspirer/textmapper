package net.sf.lapg.templates.internal.ui.editor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorManager {
	private static final String PREF_KEYWORDS = "hl_keywords";

	private static final String PREF_TERMINALS = "hl_terminals";

	private static final String PREF_TEMPLATE = "hl_define";

	private static final String PREF_STRING = "hl_string";

	private static final String PREF_TEXT = "hl_text";

	private static final String PREF_OTHER = "hl_other";


	private final Map<RGB, Color> ivColorTable = new HashMap<RGB, Color>();

	private final IPreferenceStore preferenceStore;

	public ColorManager(IPreferenceStore preferenceStore) {
		assert preferenceStore != null;
		this.preferenceStore = preferenceStore;
	}

	public void initializeDefaults() {
		initializeDefaultValues(preferenceStore);
	}

	/**
	 * Release all of the color resources held onto by the receiver.
	 */
	public void dispose() {
		for (Color c : ivColorTable.values()) {
			c.dispose();
		}
	}

	/**
	 * Return the Color that is stored in the Color table as rgb. Create new
	 * entry, if none can be found.
	 *
	 * @param rgb
	 *            RGB color to lookup from HashMap
	 */
	protected Color getColor(final RGB aRgb) {
        Color color = ivColorTable.get(aRgb);
        if (color == null) {
            color = createColor(aRgb);
            ivColorTable.put(aRgb, color);
        }
        return color;
    }

	protected Color createColor(final RGB aColor) {
		return new Color(Display.getCurrent(), aColor);
	}

	public Color getKeywordsColor() {
    	return getColor(PreferenceConverter.getColor(preferenceStore, PREF_KEYWORDS));
    }

	public Color getTerminalsColor() {
    	return getColor(PreferenceConverter.getColor(preferenceStore, PREF_TERMINALS));
    }

	public Color getTemplateColor() {
    	return getColor(PreferenceConverter.getColor(preferenceStore, PREF_TEMPLATE));
    }

	public Color getStringColor() {
    	return getColor(PreferenceConverter.getColor(preferenceStore, PREF_STRING));
    }

	public Color getOtherColor() {
    	return getColor(PreferenceConverter.getColor(preferenceStore, PREF_OTHER));
    }

	public Color getTextColor() {
    	return getColor(PreferenceConverter.getColor(preferenceStore, PREF_TEXT));
    }

	public final static void initializeDefaultValues(final IPreferenceStore store) {
		PreferenceConverter.setDefault(store, PREF_KEYWORDS, new RGB(127, 0, 85));
		PreferenceConverter.setDefault(store, PREF_TERMINALS, new RGB(100, 100, 100));
		PreferenceConverter.setDefault(store, PREF_TEMPLATE, new RGB(127, 0, 85));
		PreferenceConverter.setDefault(store, PREF_STRING, new RGB(42, 0, 255));
		PreferenceConverter.setDefault(store, PREF_OTHER, new RGB(0, 0, 0));
		PreferenceConverter.setDefault(store, PREF_TEXT, new RGB(42, 0, 255));
	}
}
