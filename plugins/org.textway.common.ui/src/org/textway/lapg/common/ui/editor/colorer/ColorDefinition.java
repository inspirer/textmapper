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
package org.textway.lapg.common.ui.editor.colorer;

import org.eclipse.swt.graphics.RGB;

public class ColorDefinition {

	private final String id;
	private final String label;
	private final String category;
	private final boolean canBeDisabled;
	private final RGB rgb;
	private final boolean bold;
	private final boolean italic;
	private final boolean underline;
	private final boolean strikethrough;

	public ColorDefinition(String id, String label, String category, boolean canBeDisabled, RGB rgb, boolean bold,
			boolean italic, boolean underline, boolean strikethrough) {
		this.id = id;
		this.label = label;
		this.category = category;
		this.canBeDisabled = canBeDisabled;
		this.rgb = rgb;
		this.bold = bold;
		this.italic = italic;
		this.underline = underline;
		this.strikethrough = strikethrough;
	}

	public String getId() {
		return id;
	}

	public String getLabel() {
		return label;
	}

	public String getCategory() {
		return category;
	}

	public boolean isCanBeDisabled() {
		return canBeDisabled;
	}

	public RGB getRgb() {
		return rgb;
	}

	public boolean isBold() {
		return bold;
	}

	public boolean isItalic() {
		return italic;
	}

	public boolean isUnderline() {
		return underline;
	}

	public boolean isStrikethrough() {
		return strikethrough;
	}
}
