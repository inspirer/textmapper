package net.sf.lapg.input;

import net.sf.lapg.templates.api.ILocatedEntity;

public class CAction implements ILocatedEntity {

	private int line;
	private String contents;

	public CAction(int line, String contents) {
		this.line = line;
		this.contents = contents;
	}

	public String getLocation() {
		return "line:" + line;
	}

	public String getContents() {
		return contents;
	}

	int getLine() {
		return line;
	}
}
