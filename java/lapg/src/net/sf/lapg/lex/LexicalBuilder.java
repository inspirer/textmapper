package net.sf.lapg.lex;

import net.sf.lapg.Lexem;

public class LexicalBuilder {

	class State {
		State next, hash;
		int number;
		//short []change;
		int[] set;
	};

	class Result {
		int[] groupset;
		int nstates, nchars, nterms;
		String[] lact;
		//int *lnum, *char2no;
		//State **dta;
	};

	// current database variables
	int[] symbols;
	int[] sym, stack;
	int lexemerrors, pool_used, pool_size;
//	int *setpool;

//	int *group, groupset[BITS];
	int nsit, nterms, sym_used;
	int[] lnum, lprio, lsym, lindex, llen, ljmpset;
	int[][] ljmp;
	String[] lact, lname;

	// generate-time variables
	int characters;
	int[] char2no, no2char;

	int states;
	State[] hash;
	State first, last, current;
	int[] clsr, cset;

	private LexicalBuilder(Lexem[] lexems) {
		
	}
	
	private int  add_set( int[] state, int size, int sum ) {
		return 0;
	}
	
	private void closure( int[] set, int /*&*/length ) {
		
	}
	
	private void BuildJumps() {
		
	}
	
	private void BuildArrays() {
		
	}
	
	private int BuildStates() {
		return 0;
	}
}
