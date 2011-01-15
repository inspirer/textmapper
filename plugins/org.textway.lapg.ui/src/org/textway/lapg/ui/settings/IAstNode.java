package org.textway.lapg.ui.settings;

import org.textway.lapg.ui.settings.SettingsTree.TextSource;

public interface IAstNode {
	int getOffset();
	int getEndOffset();
	TextSource getInput();
	//void accept(Visitor v);
}
