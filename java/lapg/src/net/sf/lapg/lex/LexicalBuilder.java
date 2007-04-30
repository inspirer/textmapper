package net.sf.lapg.lex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import net.sf.lapg.IError;
import net.sf.lapg.Lexem;
import net.sf.lapg.LexerTables;
import net.sf.lapg.common.FormatUtil;

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
	int debuglev;
	
	// lexical analyzer description
	int nsit, nterms, totalgroups;
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


	private LexicalBuilder(Lexem[] lexems, IError err, int debuglev) {
		this.err = err;
		this.debuglev = debuglev;
		this.myLexems = lexems;
	}
	
	private int add_set( int[] state, int size, int sum ) {
		return 0;
	}
	
	private void closure( int[] set, int /*&*/length ) {
		
	}

	// fills: ljmp
	private void buildJumps() {
		int i, k, lex;
		int[] stack = new int[LexConstants.MAX_DEEP];

		if( debuglev >= 2 )
			err.debug( "\nLexem jumps:\n" );

		for( lex = 0; lex < nterms; lex++ ) {

			int[] jumps = ljmp[lex];
			int jmpset = ljmpset[lex];
			int cd = -1, len = llen[lex] - 1;
			int sym_index = lindex[lex];

			// generate initial jumps
			for( i = 0; i < len; i++ ) switch( lsym[sym_index+i]&LexConstants.MASK ) {
				case LexConstants.LBR: // (
					stack[++cd] = i;
					jumps[i*jmpset + (i+1)/LexConstants.BITS] |= (1<<((i+1)%LexConstants.BITS));
					break;

				case LexConstants.RBR: // )
					jumps[i*jmpset + (i+1)/LexConstants.BITS] |= (1<<((i+1)%LexConstants.BITS));

					switch( (lsym[sym_index+i]>>29)&3 ) {
						case 1: // +
							jumps[(i+1)*jmpset + (stack[cd])/LexConstants.BITS] |= (1<<((stack[cd])%LexConstants.BITS));
							break;
						case 2: // *
							jumps[(i+1)*jmpset + (stack[cd])/LexConstants.BITS] |= (1<<((stack[cd])%LexConstants.BITS)); 
							jumps[stack[cd]*jmpset + (i+1)/LexConstants.BITS] |= (1<<((i+1)%LexConstants.BITS));
							break;
						case 3: // ?
							jumps[stack[cd]*jmpset + (i+1)/LexConstants.BITS] |= (1<<((i+1)%LexConstants.BITS));
							break;
					}
					cd--;
					break;

				case LexConstants.OR:  // |
					k = lsym[sym_index+stack[cd]]&0xffff;
					jumps[i*jmpset + (k+1)/LexConstants.BITS] |= (1<<((k+1)%LexConstants.BITS));
					jumps[stack[cd]*jmpset + (i+1)/LexConstants.BITS] |= (1<<((i+1)%LexConstants.BITS));
					break;

				case LexConstants.SPL:  // forbid two steps back
					jumps[i*jmpset + (i+1)/LexConstants.BITS] |= (1<<((i+1)%LexConstants.BITS));
					break;

				default: // SYM, ANY, SET
					switch( (lsym[sym_index+i]>>29)&3 ) {
						case 1: // +
							jumps[(i+1)*jmpset + (i)/LexConstants.BITS] |= (1<<((i)%LexConstants.BITS));
							break;
						case 2: // *
							jumps[(i+1)*jmpset + (i)/LexConstants.BITS] |= (1<<((i)%LexConstants.BITS)); 
							jumps[i*jmpset + (i+1)/LexConstants.BITS] |= (1<<((i+1)%LexConstants.BITS));
							break;
						case 3: // ?
							jumps[i*jmpset + (i+1)/LexConstants.BITS] |= (1<<((i+1)%LexConstants.BITS));
							break;
					}
					break;
			}

			// transitive closure of jumps
			int j, e;
			for( i = 0; i <= len; i++ )
				for( j = 0; j <= len; j++ )
					if( ((jumps[jmpset*j + (i)/LexConstants.BITS]&(1<<((i)%LexConstants.BITS)))!=0) )
						for( e = 0; e <= len; e++ )
							if( ((jumps[jmpset*i + (e)/LexConstants.BITS]&(1<<((e)%LexConstants.BITS)))!=0) )
								jumps[jmpset*j + (e)/LexConstants.BITS] |= (1<<((e)%LexConstants.BITS));

			// reflexive
			for( i = 0; i <= len; i++ )
				jumps[jmpset*i + (i)/LexConstants.BITS] |= (1<<((i)%LexConstants.BITS));

			// extended debug information
			if( debuglev >= 2 ) {
				err.debug( FormatUtil.asDecimal(lex, 2, ' ') + ": " );
				for( i = 0; i < len; i++ ) {
					err.debug(" ("+i+":");
					for( k = 0; k <= len; k++ )
						if ((jumps[i*jmpset + (k)/LexConstants.BITS]&(1<<((k)%LexConstants.BITS)))!=0) 
							err.debug(" " + k);
					err.debug( ") " );
					switch( lsym[sym_index+i]&LexConstants.MASK ) {
						case LexConstants.LBR: err.debug( "LEFT("+(lsym[sym_index+i]&0xffff)+")" );break;
						case LexConstants.RBR: err.debug( "RIGHT" );break;
						case LexConstants.OR:  err.debug( "OR" );break;
						case LexConstants.SPL: err.debug( "SPLIT" );break;
						case LexConstants.ANY: err.debug( "ANY" );break; 
						case LexConstants.SYM:
							if( (lsym[sym_index+i]&0xff) < 127 && (lsym[sym_index+i]&0xff) > 32 )
								err.debug( "\'"+(char)(lsym[sym_index+i]&0xff)+"\'" ); 
							else
								err.debug( "#" + FormatUtil.asHex(lsym[sym_index+i]&0xff, 2) );
							break;
						default:  err.debug( "SET#" + (lsym[sym_index+i]&~LexConstants.HIGH_STORAGE)/LexConstants.SIZE_SYM ); break;
					}
					switch( (lsym[sym_index+i]>>29)&3 ) {
						case 1: err.debug( "+" );break;
						case 2: err.debug( "*" );break;
						case 3: err.debug( "?" );break;
					}
				}
				err.debug("  ["+lname[lex]+","+lnum[lex]+"]\n");
			}
		}

		if( debuglev >= 2 )
			err.debug("\n");
	}
	
	// fills: char2no, no2char, characters
	private void buildArrays() {
		int i, e;
		
		char2no = new int[256];
		no2char = new int[256];
		
		for( i = 0; i < 256; i++ ) 
			char2no[i] = 1;

		for( characters = e = 0; e < LexConstants.SIZE_SYM; e++ )
			if( symbols[e] != 0 )
				for( i = 0; i < LexConstants.BITS; i++ )
					if( (symbols[e] & (1<<i)) != 0 ) {
						no2char[characters] = e*LexConstants.BITS+i;
						char2no[e*LexConstants.BITS+i] = characters++;
					}
	}
	
	private boolean buildStates() {
//		int i, e, k;
//		int *cs, *next, nnext;
//		int[] toshift = new int[SIZE_SYM];
//
//		// allocate temporary storage
//		clsr = new int[nsit];
//		next = new int[nsit+1];
//		nnext = 0;
//
//		// fix lindex and allocate temporary set
//		inc_realloc( (void**)&lindex, nterms, sizeof(int) );
//		lindex[nterms] = nsit;
//		for( e = 1, i = 0; i < nterms; i++ )
//			if( ljmpset[i] > e ) e = ljmpset[i];
//		cset = new int[e];
//
//		// create first set
//		for( i = 0; i < nterms; i++ ) 
//			if( group[i] & 1 )
//				next[nnext++] = lindex[i];
//		next[nnext] = -1;
//		closure( next, nnext );
//		for( e = 0, cs = next; *cs >= 0; cs++ ) e += *cs;
//		e = (unsigned)e % HASH_SIZE;
//		groupset[0] = 0;
//
//		// create state
//		states = 1;
//		Arrays.fill(hash, null);
//		
//		current = first = last = (State *) new char[ sizeof(State) + nnext*sizeof(int) ];
//		first.number = 0;
//		first.hash = first.next = NULL;
//		hash[e] = first;
//		for( i = 0; i <= nnext; i++ )
//			first.set[i] = next[i];
//
//		// create left group states
//		for( k = 1; k < BITS; k++ ) {
//			if( totalgroups & (1<<k) ) {
//				for( nnext = i = 0; i < nterms; i++ ) 
//					if( group[i] & (1<<k) )
//						next[nnext++] = lindex[i];
//				next[nnext] = -1;
//				closure( next, nnext );
//				for( e = 0, cs = next; *cs >= 0; cs++ ) e += *cs;
//				groupset[k] = add_set( next, nnext, (unsigned)e % HASH_SIZE );
//			} else {
//				groupset[k] = (unsigned)-1;
//			}
//		}
//
//		// generate states
//		while( current ) {
//
//			// first of all we must search if there any lexem have been read already
//			int lexnum = -1;
//			memset( toshift, 0, (((256)+BITS-1)/BITS)*sizeof(int) );
//			for( cs = current.set; *cs >= 0; cs++ ) {
//				if( lsym[*cs] < 0 && lsym[*cs] >= -MAX_LEXEMS ) {
//
//					// end of some regexp found
//					final int nlex = -1-lsym[*cs];
//					if( lexnum != -1 && lexnum != nlex ) {
//
//						if( lprio[nlex] == lprio[lexnum] ) {
//							err.error("lex: two lexems are identical: "+lname[lexnum]+" and "+lname[nlex]+"\n");
//							lexemerrors++;
//
//						} else if( lprio[nlex] > lprio[lexnum] ) {
//							if( debuglev ) 
//								err.warn("fixed: %s > %s\n", lname[nlex], lname[lexnum] );
//							lexnum = nlex;
//
//						} else if( debuglev ) 
//							err.warn("fixed: %s > %s\n", lname[lexnum], lname[nlex] );
//
//					} else lexnum = nlex;
//			
//				} else {
//					switch( lsym[*cs]&MASK ) {
//						case SYM: 
//							toshift[(lsym[*cs]&0xff)/BITS] |= (1<<((lsym[*cs]&0xff)%BITS));
//							break;
//						case ANY: 
//							for( i = 1; i < SIZE_SYM; i++ )
//								toshift[i] = ~0;
//							toshift[0] |= ~((1<<'\n')+(1));
//							break;
//						default: 
//							e = lsym[*cs]&~HIGH_STORAGE;
//							for( i = 0; i < SIZE_SYM; i++, e++ )
//								toshift[i] |= setpool[e];
//							break;
//					}
//				}
//			}
//
//			// check for the empty lexem
//			if( current == first && lexnum != -1 ) {
//				err.error( 0, "lex: lexem is empty: `%s`\n", lname[lexnum] );
//				lexemerrors++;
//			}
//
//
//			// allocate new change table
//			current.change = new short[characters];
//
//			// try to shift all available symbols
//			for( i = 0; i < characters; i++ ) {
//				if ((toshift[(no2char[i])/BITS]&(1<<((no2char[i])%BITS)))!=0) {
//					int *p, *t, *o, l, sym = no2char[i];
//
//					// create new state
//					for( t = next, p = current.set; *p >= 0; p++ ) {
//						l = lsym[*p];
//
//						if( (l&MASK) == ANY ) {
//							if( sym != '\n' ) 
//								*t++ = *p + 1;
//						} else if( (l&MASK) == SYM ) {
//							if( sym == (l&0xff) ) 
//								*t++ = *p + 1;
//						} else if( l >= 0 ) {
//							o = &setpool[l&~HIGH_STORAGE];
//							if ((o[(sym)/BITS]&(1<<((sym)%BITS)))!=0) 
//								*t++ = *p + 1;
//						}
//					}
//					nnext = t - next;
//
//					// closure
//					*t = -1;
//					closure( next, nnext );
//
//					// save new state
//					for( l = e = 0; e < nnext; e++ )
//						l += next[e];
//
//					current.change[i] = add_set( next, nnext, (unsigned)l % HASH_SIZE );
//
//					// Have we exceeded the limits?
//					if( current.change[i] == -1 ) {
//						err.error( "lex: lexical analyzer is too big ...\n" );
//						return false;
//					}
//
//				} else { 
//					current.change[i] = (lexnum>=0) ? -2-lnum[lexnum] : -1;
//				}
//			}
//
//			ASSERT( current.change[0] < 0 );
//
//			// next state
//			current = current.next;
//		}
//
//		first.change[0] = -2;

		return true;
	}

	/**
	 *   Fills initial arrays from lexems descriptions
	 */
	private boolean prepare() {
		RegexpParser rp = new RegexpParser(err);
		nsit = 0;
		ArrayList<int[]> syms = new ArrayList<int[]>();
		nterms = myLexems.length;
		
		if( nterms >= LexConstants.MAX_LEXEMS) {
			err.error( "lex: too much lexems\n" );
			return false;
		}

		totalgroups = 0;
		lnum = new int[nterms];
		lprio = new int[nterms];
		group = new int[nterms];
		lindex = new int[nterms];
		llen = new int[nterms];
		ljmpset = new int[nterms];
		ljmp = new int[nterms][];
		lname = new String[nterms];
		lact = new String[nterms];

		for( int i = 0; i < nterms; i++ ) {
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
			lindex[i] = nsit;
			llen[i] = lexem_sym.length;
			ljmpset[i] = (((lexem_sym.length)+LexConstants.BITS-1)/LexConstants.BITS);
			ljmp[i] = new int[lexem_sym.length*ljmpset[i]];
			lname[i] = l.name;
			lact[i] = l.action;

			Arrays.fill(ljmp[i], 0);
			syms.add(lexem_sym);
			nsit += lexem_sym.length;
		}
		lsym = new int[nsit];
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

	private LexerTables generate() {

		if( myLexems.length == 0 ) {
			err.error( "lex: no lexems\n" );
			return null;
		}

		if( !prepare())
			return null;
	
		buildJumps();
		buildArrays();
		buildStates();

		// TODO
		return null;
	}

	/**
	 *  Generates lexer tables from lexems descriptions
	 */
	public static LexerTables compile(Lexem[] lexems, IError err, int debuglev) {
		LexicalBuilder lb = new LexicalBuilder(lexems, err, debuglev);
		return lb.generate();
	}
}
