package net.sf.lapg.input;

import net.sf.lapg.api.Action;
import net.sf.lapg.templates.api.ILocatedEntity;

public class CAction implements Action, ILocatedEntity {

	private final String contents;

	private final String input;
	private final int line;

	public CAction(String contents, String input, int line) {
		this.contents = contents;
		this.input = input;
		this.line = line;
	}

	public String getLocation() {
		return input + "," + line;
	}

	public String getContents() {
		return contents;
	}

	int getLine() {
		return line;
	}

	@Override
	public String toString() {
		return contents;
	}
}
