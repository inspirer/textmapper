package org.textway.lapg.ui.settings;

import java.util.List;
import org.textway.lapg.ui.settings.SettingsTree.TextSource;

public class AstInput extends AstNode {

	private List<AstSettings> settingsList;

	public AstInput(List<AstSettings> settingsList, TextSource input, int start, int end) {
		super(input, start, end);
		this.settingsList = settingsList;
	}

	public List<AstSettings> getSettingsList() {
		return settingsList;
	}
}
