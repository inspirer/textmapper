package net.sf.lapg;

public class Lexem {
	public final int index;
	public final String name, regexp;
	public final String action;
	public final int priority;
	public final int groups;
	
	public Lexem(int index, String name, String regexp, String action, int priority, int groups) {
		this.index = index;
		this.name = name;
		this.regexp = regexp;
		this.action = action;
		this.priority = priority;
		this.groups = groups;
	}
}
