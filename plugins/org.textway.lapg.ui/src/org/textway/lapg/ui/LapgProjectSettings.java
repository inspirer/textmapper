package org.textway.lapg.ui;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.textway.lapg.gen.LapgOptions;
import org.textway.lapg.ui.settings.SettingsPersister;

public class LapgProjectSettings {

	public static final IPath SETTINGS_FILE = new Path(".lapg");

	private final IFile myFile;
	private volatile Map<IPath, LapgOptions> mySettings;

	public LapgProjectSettings(IProject project) {
		myFile = project.getFile(SETTINGS_FILE);
	}
	
	public IProject getProject() {
		return myFile.getProject();
	}

	public Map<IPath, LapgOptions> getSettings() {
		Map<IPath, LapgOptions> result = mySettings;
		if(result == null) {
			result = readSettings();
			mySettings = result;
		}
		return result;
	}
	
	public void reloadSettings() {
		mySettings = null;
	}
	
	private Map<IPath, LapgOptions> readSettings() {
		if (!myFile.exists()) {
			return Collections.emptyMap();
		}

		try {
			String content = FileUtil.getStreamContents(myFile.getContents(), myFile.getCharset());
			return SettingsPersister.load(content);
		} catch (IOException ex) {
		} catch (CoreException e) {
		}
		return Collections.emptyMap();
	}
}
