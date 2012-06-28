package org.textmapper.lapg.ui.settings;

import org.textmapper.lapg.ui.settings.SettingsTree.TextSource;

public interface IAstNode {
	int getOffset();
	int getEndOffset();
	TextSource getInput();
	//void accept(Visitor v);
}
