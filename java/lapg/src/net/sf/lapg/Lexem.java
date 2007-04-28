package net.sf.lapg;

public class Lexem {
	public final int index;
	public final String name, regexp;
	public final String action;
	public final int priority;
	
	public Lexem(int index, String name, String regexp, String action, int priority) {
		this.index = index;
		this.name = name;
		this.regexp = regexp;
		this.action = action;
		this.priority = priority;
	}
}
