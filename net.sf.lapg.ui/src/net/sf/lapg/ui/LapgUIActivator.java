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
package net.sf.lapg.ui;

import net.sf.lapg.common.ui.editor.colorer.DefaultColorManager;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class LapgUIActivator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "net.sf.lapg.ui";

	private static LapgUIActivator myDefault;

	public LapgUIActivator() {
		myDefault = this;
	}

	public static LapgUIActivator getDefault() {
		return myDefault;
	}

	public static void log(Throwable e) {
		logError("Internal error", e);
	}

	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void logError(String text) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, text));
	}

	public static void logError(String text, Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, 0, text, e));
	}

	private DefaultColorManager fColorManager;

	public DefaultColorManager getColorManager() {
		if (fColorManager == null) {
			fColorManager = new DefaultColorManager(true);
		}
		return fColorManager;
	}
}
