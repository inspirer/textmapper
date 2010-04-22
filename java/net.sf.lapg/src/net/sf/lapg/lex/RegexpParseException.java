package net.sf.lapg.lex;

public class RegexpParseException extends Exception {
	private static final long serialVersionUID = -8052552834958196703L;

	private final int errorOffset;

	public RegexpParseException(String message, int offset) {
		super(message);
		this.errorOffset = offset;
	}

	public int getErrorOffset() {
		return errorOffset;
	}
}
