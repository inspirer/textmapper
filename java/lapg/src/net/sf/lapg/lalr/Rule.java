package net.sf.lapg.lalr;

public class Rule {

	public final int left;
	public final int[] right;
	public final int line;
	public final int prio;
	public final String action;
	
	public Rule(int left, int line, int prio, String action, int[] right) {
		this.left = left;
		this.line = line;
		this.prio = prio;
		this.action = action;
		this.right = right;
	}
}