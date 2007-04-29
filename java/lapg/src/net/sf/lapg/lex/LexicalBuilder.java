package net.sf.lapg.lex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import net.sf.lapg.IError;
import net.sf.lapg.Lexem;
import net.sf.lapg.LexerTables;

public class LexicalBuilder {

	class State {
		State next, hash;
		int number;
		//short []change;
		int[] set;
	};

	// initial information
	private Lexem[] myLexems;
	private IError err;
	
	// lexical analyzer description
	int totalgroups;
	private int[] symbols, setpool;
	private int[] lsym;

	private int[] lnum, lprio, group, lindex, llen, ljmpset;
	private int[][] ljmp;
	private String[] lname, lact;
	
	// generate-time variables
	int characters;
	int[] char2no, no2char;

	int states;
	State[] hash;
	State first, last, current;
	int[] clsr, cset;

	//int groupset[BITS];


	private LexicalBuilder(Lexem[] lexems, IError err) {
		this.err = err;
		this.myLexems = lexems;
	}
	
	private int add_set( int[] state, int size, int sum ) {
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

	private LexerTables generate() {

		if( myLexems.length == 0 ) {
			err.error( "lex: no lexems\n" );
			return null;
		}

		if( !prepare())
			return null;
		
		// TODO
		return null;
	}

	private boolean prepare() {
		RegexpParser rp = new RegexpParser(err);
		int syms_size = 0;
		ArrayList<int[]> syms = new ArrayList<int[]>();
		
		if( myLexems.length >= LexConstants.MAX_LEXEMS) {
			err.error( "lex: too much lexems\n" );
			return false;
		}

		totalgroups = 0;
		lnum = new int[myLexems.length];
		lprio = new int[myLexems.length];
		group = new int[myLexems.length];
		lindex = new int[myLexems.length];
		llen = new int[myLexems.length];
		ljmpset = new int[myLexems.length];
		ljmp = new int[myLexems.length][];
		lname = new String[myLexems.length];
		lact = new String[myLexems.length];

		for( int i = 0; i < myLexems.length; i++ ) {
			Lexem l = myLexems[i];
			totalgroups |= l.groups;
			
			if( l.groups == 0 ) {
				err.error( "lex: lexem "+l.name+" does not belong to groups\n" );
				return false;
			}
			
			if( l.name.equals("error") ) {
				err.error( "lex: error token must be defined without regular expression\n" );
				return false;
			}

			int[] lexem_sym =  rp.compile(l.index, l.name, l.regexp);

			lnum[i] = l.index;
			lprio[i] = l.priority;
			group[i] = l.groups;
			lindex[i] = syms_size;
			llen[i] = lexem_sym.length;
			ljmpset[i] = (((lexem_sym.length)+LexConstants.BITS-1)/LexConstants.BITS);
			ljmp[i] = new int[lexem_sym.length*ljmpset[i]];
			lname[i] = l.name;
			lact[i] = l.action;

			Arrays.fill(ljmp[i], 0);
			syms.add(lexem_sym);
			syms_size += lexem_sym.length;
		}
		lsym = new int[syms_size];
		int e = 0;
		for( Iterator<int[]> it = syms.iterator(); it.hasNext(); ) {
			int[] from = it.next();
			for( int i = 0; i < from.length; i++ )
				lsym[e++] = from[i];
		}

		if( (totalgroups & 1 ) == 0 ) {
			err.error( "lex: no lexems in the first group\n" );
			return false;
		}

		setpool = rp.getSetpool();
		symbols = rp.getSymbolSet();
		return true;
	}

	/**
	 *  Generates lexer tables from lexems descriptions
	 */
	public static LexerTables compile(Lexem[] lexems, IError err) {
		LexicalBuilder lb = new LexicalBuilder(lexems, err);
		return lb.generate();
	}
}
