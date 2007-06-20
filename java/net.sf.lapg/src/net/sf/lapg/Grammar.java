package net.sf.lapg;

import java.util.Vector;


public class Grammar {
	
	public final Vector<Symbol> syms;
	public final Vector<Rule> rules;
	public final Vector<Integer> prio;
	public final int nterms, situations;
	public final int input, eoi, errorn;

	public Grammar(Vector<Symbol> syms, Vector<Rule> rules, Vector<Integer> prio, int nterms, int situations, int input, int eoi, int errorn) {
		this.syms = syms;
		this.rules = rules;
		this.nterms = nterms;
		this.situations = situations;
		this.input = input;
		this.eoi = eoi;
		this.errorn = errorn;
		this.prio = prio;
	}
}
