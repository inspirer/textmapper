package org.textmapper.lapg.ui.settings;

import java.util.List;
import org.textmapper.lapg.ui.settings.SettingsTree.TextSource;

public class AstOption extends AstNode {

	private boolean isVardef;
	private String identifier;
	private String scon;
	private List<String> stringList;

	public AstOption(boolean isVardef, String identifier, String scon, List<String> stringList, TextSource input, int start, int end) {
		super(input, start, end);
		this.isVardef = isVardef;
		this.identifier = identifier;
		this.scon = scon;
		this.stringList = stringList;
	}

	public boolean getIsVardef() {
		return isVardef;
	}
	public String getIdentifier() {
		return identifier;
	}
	public String getScon() {
		return scon;
	}
	public List<String> getStringList() {
		return stringList;
	}
}
