package org.textway.lapg.ui.settings;

import java.util.List;
import org.textway.lapg.ui.settings.SettingsTree.TextSource;

public class AstSettings extends AstNode {

	private String scon;
	private List<AstOption> optionsList;

	public AstSettings(String scon, List<AstOption> optionsList, TextSource input, int start, int end) {
		super(input, start, end);
		this.scon = scon;
		this.optionsList = optionsList;
	}

	public String getScon() {
		return scon;
	}
	public List<AstOption> getOptionsList() {
		return optionsList;
	}
}
