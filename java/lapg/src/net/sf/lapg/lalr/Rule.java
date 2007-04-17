package net.sf.lapg.lalr;

public class Rule {

	public int left, line, prio;
	public String action;
	public int[] right;
	
	public Rule(int left, int line, int prio, String action, int[] right) {
		this.left = left;
		this.line = line;
		this.prio = prio;
		this.action = action;
		this.right = right;
	}
}