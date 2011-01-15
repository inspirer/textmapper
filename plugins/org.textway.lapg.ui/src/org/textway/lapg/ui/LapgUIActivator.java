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
package org.textway.lapg.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.textway.lapg.common.ui.editor.colorer.DefaultColorManager;

public class LapgUIActivator extends AbstractUIPlugin {

	public static final String PLUGIN_ID = "org.textway.lapg.ui";

	private static LapgUIActivator myDefault;
	private ListenerList mySettingsListeners = new ListenerList();
	private Map<IProject, LapgProjectSettings> myProjectSettings = new HashMap<IProject, LapgProjectSettings>();

	public LapgUIActivator() {
		myDefault = this;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(mySettingsReloader);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(mySettingsReloader);
		super.stop(context);
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

	public void addSettingsChangedListener(LapgSettingsListener listener) {
		mySettingsListeners.add(listener);
	}

	public void removeSettingsChangedListener(LapgSettingsListener listener) {
		mySettingsListeners.remove(listener);
	}

	private void notifySettingsChanged(Set<IProject> settings) {
		Object[] listeners = mySettingsListeners.getListeners();
		for (int i = 0; i < listeners.length; ++i) {
			((LapgSettingsListener) listeners[i]).settingsChanged(settings);
		}
	}

	public LapgProjectSettings getProjectSettings(IProject project) {
		synchronized (myProjectSettings) {
			LapgProjectSettings result = myProjectSettings.get(project);
			if (result == null) {
				result = new LapgProjectSettings(project);
				myProjectSettings.put(project, result);
			}
			return result;
		}
	}

	private final IResourceChangeListener mySettingsReloader = new IResourceChangeListener() {
		public void resourceChanged(IResourceChangeEvent event) {
			if (event == null || event.getDelta() == null) {
				return;
			}
			Set<LapgProjectSettings> affectedSettings = null;
			IResourceDelta rootDelta = event.getDelta();
			synchronized (myProjectSettings) {
				for (IResourceDelta projectDelta : rootDelta.getAffectedChildren()) {
					IProject affectedProject = (IProject) projectDelta.getResource();
					if (isRemovedOrClosed(projectDelta)) {
						myProjectSettings.remove(affectedProject);
					} else {
						IResourceDelta configFileDelta = projectDelta.findMember(LapgProjectSettings.SETTINGS_FILE);
						if (configFileDelta != null && myProjectSettings.containsKey(affectedProject)
								&& affectsFile(configFileDelta)) {
							if (affectedSettings == null) {
								affectedSettings = new HashSet<LapgProjectSettings>();
							}
							affectedSettings.add(getProjectSettings(affectedProject));
						}
					}
				}
			}

			if (affectedSettings == null) {
				return;
			}
			Set<IProject> affectedProjects = new HashSet<IProject>(affectedSettings.size());
			for (LapgProjectSettings settings : affectedSettings) {
				settings.reloadSettings();
				affectedProjects.add(settings.getProject());
			}
			notifySettingsChanged(affectedProjects);
		}

		private boolean affectsFile(IResourceDelta fileDelta) {
			if ((fileDelta.getKind() & (IResourceDelta.ADDED | IResourceDelta.REMOVED)) > 0) {
				return true;
			}
			if ((fileDelta.getFlags() & (IResourceDelta.CONTENT | IResourceDelta.ENCODING | IResourceDelta.SYNC
					| IResourceDelta.TYPE | IResourceDelta.REPLACED)) > 0) {
				return true;
			}
			return false;
		}

		private boolean isRemovedOrClosed(IResourceDelta projectDelta) {
			if (projectDelta.getKind() == IResourceDelta.REMOVED) {
				return true;
			}
			if ((projectDelta.getFlags() & IResourceDelta.OPEN) > 0) {
				return !projectDelta.getResource().isAccessible();
			}
			return false;
		}
	};

	public interface LapgSettingsListener {
		void settingsChanged(Set<IProject> settings);
	}
}
