package net.sf.lapg.lalr;

import java.lang.reflect.Array;
import java.text.MessageFormat;
import java.util.Arrays;

public class GrammarBuilder {
	
	public static final int BITS = 32;
	public static final int STATE_TABLE_SIZE = 1037;
	public static final int MAX_WORD = 0x7ff0;

	public class Symbol {
		String name, type;
		int  opt, sibling;
		int  length, rpos;
		boolean empty, term, good, employed, defed, 
				 temp, is_attr, has_attr;
	};

	public class State {
		int  fromstate, symbol, number, nshifts, nreduce;
		State link, next;
		short[] shifts, reduce;
		boolean LR0;
		short[] elems;
	};

	public class Result {
		Symbol sym;
		int rules, nsyms, nterms, nstates, errorn;
		int[] rleft, rright, rindex, rprio, rlines;
		String[] raction;
		short[] sym_goto, sym_from, sym_to, action_table;
		int[] action_index;
		int nactions;
	};

	public class Short {
		short value;
		Short next;
	};

	// init
	private int debuglev;
	private IError err;
	private int errors;

	// grammar information
	private Symbol[] sym;
	private int nsyms, nterms, input, eoi, errorn;

	private int[] rleft, rindex, rright, rprio, rlines, priorul;
	private int nprio;
	private String[] raction;
	private int rules, situations, right_used;

	// LR0 engine
	private int nvars;
	private int termset, varset, ruleset;
	private int nstates;
	private int[] firsts /*nvars x nvars*/, ruleforvar /* nvars x rules */;
	private int[][] derives /* nvars: list of rules */;
	private State[] state;

	private short[] toreduce, closure /* [situations] */;
	private int closureend /* size of closure */;
	private short[][] symbase;
	private int[] symbasesize;
	private short[] symcanshift;
	private int[]   closurebit /*set of rules*/;
	private int 	ntoreduce, ntoshift;
	private State[] table; 
	private State current, last, first;

	// last generation
	private int[]   action_index;
	private int nactions, nactions_used;
	private short[] action_table;
	
	// LR0
	
	int LR0() {
		allocate_data();
		build_derives();
		build_sets();
		initializeLR0();

		while( current != null ) {
			build_closure( current.elems );
			if( !process_state() ) {
				err.error( 0, "syntax analyzer is too big ...\n" );
				state = new State[nstates];
				for( State t = first; t != null; t = t.next )
					state[t.number] = t;
				return 0;
			}
			current = current.next;
		}

		add_final_states();
		show_debug();
		return 1;
	}
	
	void allocate_data() {
		ruleset	= (((rules)+BITS-1)/BITS);
		varset = (((nvars)+BITS-1)/BITS);
		termset = (((nterms)+BITS-1)/BITS);

		toreduce = new short[ rules + 1 ];
		closure = new short[ situations ];
		closurebit = new int[ ruleset ];
		
		table = new State[STATE_TABLE_SIZE];
		Arrays.fill(table, null);

		// state transition temporary data
		short[] symnum = new short[nsyms];
		Arrays.fill(symnum, (short)0);

		int i, count;

		for( count = i = 0; i < situations; i++ ) {
			if( rright[i] >= 0 ) { 
				count++;
				symnum[rright[i]]++;
			}
		}

		symbase = new short[nsyms][];
		symbasesize = new int[nsyms];

		for( i = 0; i < nsyms; i++ ) {
			symbase[i] = new short[symnum[i]];
		}
		symcanshift = symnum;
	}
	
	void build_derives() {
		int i, e;
		int[] q = new int[rules];
		int[] m = new int[nvars];
		int[] count = new int[nvars];

		Arrays.fill(m, -1);
		Arrays.fill(count, 0);

		for( i = rules - 1; i >= 0; i-- ) {
			e = rleft[i] - nterms;
			q[i] = m[e];
			m[e] = i;
			count[e]++;
		}

		derives = new int[nvars][];

		for( i = 0; i < nvars; i++ ) {
			int[] current = new int[count[i]];
			derives[i] = current;
			int c = 0;
			e = m[i];
			for( ; e!=-1; e = q[e] )
				current[c++] = e;
			assert c == count[i];
		}
	}
	
	void build_sets() {
		int i, e, j;
		int[] p;
		
		// firsts
		firsts = new int[ nvars * varset ];
		Arrays.fill(firsts, 0);

		for( i = 0; i < nvars; i++ ) {
			p = derives[i];
			for( int q = 0; q < p.length; q++ ) {
				e = rright[rindex[p[q]]];
				if( e >= (int)nterms )
					firsts[varset*i + (e - nterms)/BITS] |= (1<<((e - nterms)%BITS));
			}
		}

		// [j,i] && [i,e] => [j,e]
		for( i = 0; i < nvars; i++ )
			for( j = 0; j < nvars; j++ )
				if( ((firsts[varset*j + (i)/BITS]&(1<<((i)%BITS)))!=0) )
					for( e = 0; e < nvars; e++ )
						if( ((firsts[varset*i + (e)/BITS]&(1<<((e)%BITS)))!=0) )
							firsts[varset*j + (e)/BITS] |= (1<<((e)%BITS));

		// set [i,i]
		for( i = 0; i < nvars; i++ )
			firsts[varset*i + (i)/BITS] |= (1<<((i)%BITS));

		// ruleforvar
		ruleforvar = new int[ nvars * ruleset ];
		Arrays.fill(ruleforvar, 0);

		for( i = 0; i < nvars; i++ )
			for( e = 0; e < nvars; e++ )
				if( ((firsts[varset*i + (e)/BITS]&(1<<((e)%BITS)))!=0) ) {
					p = derives[e];
					for( int q = 0; q < p.length; q++ ) {
						ruleforvar[ruleset*i + (p[q])/BITS] |= (1<<((p[q])%BITS));
					}
				}
	}

	void initializeLR0() {
		nstates = 1;
		first = last = current = new State();
		current.nreduce = current.nshifts = current.number = current.symbol = current.fromstate = 0;
		current.next = current.link = null;
		current.shifts = current.reduce = null;
		current.elems[0] = -1;
	}
	
	void build_closure( short[] prev ) {
		int e;

		if( prev[0] == -1 ) {
			int from = (input-nterms) * ruleset;
			for( int i = 0; i < ruleset; i++ )
				closurebit[i] = ruleforvar[from++];

		} else {
			Arrays.fill(closurebit, 0);

			for( int i = 0; prev[i] >= 0; i++ ) {
				e = rright[prev[i]];
				if( e >= (int)nterms ) {
					int from = (e-nterms) * ruleset;
					for( int x = 0; x < ruleset; x++ )
						closurebit[x] |= ruleforvar[from++];
				}
			}
		}

		int rule = 0, prev_index = 0;
		closureend = 0;

		for( e = 0; e < ruleset; e++ ) {
			int rulebit = closurebit[e];
			if( rulebit == 0 ) {
				rule += BITS;
			} else for( e = 0; e < BITS; e++ ) {
				if( (rulebit & (1<<e)) != 0 ) {
					int index = rindex[rule];
					while( prev[prev_index] >= 0 && prev[prev_index] < index )
						closure[closureend++] = prev[prev_index++];
					closure[closureend++] = (short)index;
				}
				rule++;
			}
		}

		while( prev[prev_index] >= 0 )
			closure[closureend++] = prev[prev_index++];
	}

	State new_state( int from, int by, int hash, int size ) {
		last = last.next = new State();
		last.elems = new short[size+1];
		last.link = table[ hash%STATE_TABLE_SIZE ];
		table[ hash%STATE_TABLE_SIZE ] = last;
		last.fromstate = from;
		last.symbol = by;
		last.number = nstates++;
		last.nshifts = last.nreduce = 0;
		last.next = null;
		last.reduce = last.shifts = null;
		last.LR0 = true;
		last.elems[size] = -1;
		return last;
	}
	
	int goto_state( int symbol ) {
		short[] new_core = symbase[symbol];
		int size = symbasesize[symbol];
		int i, hash;
		State t;

		for( hash = i = 0; i < size; i++ ) 
			hash += new_core[i];
		t = table[ hash%STATE_TABLE_SIZE ];

		while( t != null ) {
			for( i = 0; i < size; i++ ) {
				if( new_core[i] != t.elems[i] )
					break;
			}

			if( i == size )
				break;

			t = t.link;
		}

		if( t == null ) {
			t = new_state( current.number, symbol, hash, size );
			for( i = 0; i < size; i++ ) t.elems[i] = new_core[i];
			t.elems[size] = -1;
		}

		return t.number;
	}
	
	boolean process_state() {
		int i, e, sym;

		ntoreduce = 0;
		Arrays.fill(symbasesize, (short)0);

		for( i = 0; i < closureend; i++ ) {
			sym = rright[closure[i]];
			if( sym >= 0 ) {
				e = symbasesize[sym];
				symbase[sym][e++] = (short)(closure[i] + 1);
				symbasesize[sym] = e;

			} else 
				toreduce[ntoreduce++] = (short)(-1-closure[i]);
		}

		ntoshift = 0;
		for( i = 0; i < nsyms; i++ ) {
			if( symbasesize[i] != 0 ) {
				symcanshift[ntoshift++] = (short)i;
			}
		}

		current.nshifts = ntoshift;
		current.shifts = ( ntoshift != 0 ) ? new short[ntoshift] : null;
		
		current.nreduce = ntoreduce;
		current.reduce = ( ntoreduce != 0 ) ? new short[ntoreduce] : null;

		for( i = 0; i < ntoshift; i++ ) {
			current.shifts[i] = (short)goto_state( symcanshift[i] );
			if( current.shifts[i] >= MAX_WORD ) return false;
		}

		for( i = 0; i < ntoreduce; i++ )
			current.reduce[i] = toreduce[i];

		current.LR0 = !(ntoreduce > 1 || (ntoshift != 0 && ntoreduce != 0) );
		return true;
	}
		
	void print_situation( int errl, int situation ) {
		int rulenum, i;
		
		for( i = situation; rright[i] >= 0; i++ );
		rulenum = -rright[i]-1;

		// left part of the rule
		err.error( errl, "  "+sym[rleft[rulenum]].name+" ::=" );

		for( i = rindex[rulenum]; rright[i] >= 0; i++ ) {
			if( i == situation ) err.error( errl, " _" );
			err.error( errl, " " + sym[rright[i]].name );
		}
		if( i == situation ) err.error( errl, " _" );
		err.error( errl, "\n" );
	}

	void print_rule( int errl, int rule ) {
		
	}
	
	void insert_shift( State t, int tostate ) {
		if( t.shifts != null ) {
			final int symbol = state[tostate].symbol;
			short[] old = t.shifts;
			int i, e, n = t.nshifts;

			t.shifts = new short[n+1];
			e = 0;
			for( i = 0; i < n && state[old[i]].symbol < symbol; i++ )
				t.shifts[e++] = old[i];
			t.shifts[e++] = (short)tostate;
			for(; i < n; i++ ) t.shifts[e++] = old[i];
			t.nshifts++;

		} else {
			t.nshifts = 1;
			t.shifts = new short[1];
			t.shifts[0] = (short)tostate;
		}			 
	}
	
	void add_final_states() {
		State next_to_final, final_state, t;
		boolean created = false;

		// search for next_to_final
		for( next_to_final = first.next;
		     next_to_final != null && next_to_final.fromstate == 0;
			 next_to_final = next_to_final.next ) {
				if( next_to_final.symbol == input )
					break;
		}

		if( next_to_final != null && next_to_final.fromstate != 0 ) 
			next_to_final = null;

		// if not found then create
		if( next_to_final == null ) {
			next_to_final = new_state( 0, input, 0, 0 );
			created = true;
		}

		final_state = new_state( next_to_final.number, eoi, 0, 0 );

		// create state array
		state = new State[nstates];
		for( t = first; t != null; t = t.next )
			state[t.number] = t;

		// insert shifts
		if( created )
			insert_shift( first, next_to_final.number );
		insert_shift( next_to_final, final_state.number );
	}
	
	void show_debug() {
		if( debuglev != 0 ) {
			err.error( 2, "\nStates\n0:\n" );

			for( State t = first; t != null; t = t.next ) {
				if( t != first )
					err.error( 2, "\n"+t.number+": (from "+t.fromstate+", "+sym[t.symbol].name+")\n");

				build_closure( t.elems );

				for( int i = 0; i < closureend; i++ ) {
					print_situation( 2, closure[i] );
				}
			}
		}

	}
	
	// LALR
	
	
	// lalr engine
	private int maxrpart, ngotos, ntgotos;
	private short[] larule, laindex;
	private Short[] lookback;
	private short[] nterm_goto, nterm_from, nterm_to;
	private short[] edge;
	private short[][] graph;
	private int[] LA /* ? -> setof(term) */, follow /* ngotos ->setof(term) */;

	void lalr()
	{
		initializeLA();
		init_goto();

		edge = new short[ngotos+1];
		graph = new short[ngotos][];

		init_follow();
		build_follow();
		show_follow();

		buildLA();
		show_lookaheads();

		edge = null;
		graph = null;

		freeLA();
		
		for( int i = nterms; i <= nsyms; i++ )
			nterm_goto[i] += ntgotos;
	}


//	 fills: laindex, larules, maxrpart; creates: lookback, LA
	void initializeLA()
	{
		int i, e, k;
		State t;

		// calculate maxrpart
		maxrpart = 2;
		for( e = i = 0; i < situations; i++ ) {
			if( rright[i] < 0 ) {
				if( e > maxrpart ) maxrpart = e;
				e = 0;
			} else e++;
		}

		// get larule size
		for( e = 0, t = first; t != null; t = t.next )
			if( !t.LR0 )
				e += t.nreduce;

		if( e == 0 ) 
			e = 1;

		// allocate
		laindex = new short[ nstates+1 ];
		laindex[nstates] = (short)e;
		larule = new short[ e ];
		lookback = new Short[ e ];
		LA = new int[ e * termset ];

		Arrays.fill(lookback, 0);
		Arrays.fill(LA, 0);

		// fills: larule, laindex
		int laind = 0;
		for( i = 0, t = first; t != null; t = t.next ) {
			laindex[t.number] = (short)i;
			if( !t.LR0 ) {
				for( k = 0; k < t.nreduce; k++ )
					larule[laind++] = t.reduce[k];
				i += t.nreduce;
			}
		}
	}


//	 fills: nterm_goto, nterm_from, nterm_to
	void init_goto()
	{
		State t;
		int i, e, symnum;
		short[] goto_nshifts;
		short[] term_goto /* nsyms */, term_from, term_to /* ngotos: state->state */;

		goto_nshifts = new short[ nsyms ];
		term_goto = new short[ nsyms + 1 ];
		
		Arrays.fill(goto_nshifts, (short)0);

		ngotos = 0;
		for( t = first; t != null; t = t.next )
			for( i = t.nshifts-1; i >= 0; i-- ) {
				symnum = state[t.shifts[i]].symbol;
				goto_nshifts[symnum]++;
				ngotos++;
			}

		for( e = i = 0; i < nsyms; i++ ) {
			term_goto[i] = (short)e;
			e += goto_nshifts[i];
			goto_nshifts[i] = term_goto[i];
		}
		term_goto[nsyms] = (short)ngotos;
		term_from = new short[ ngotos ];
		term_to = new short[ ngotos ];

		for( t = first; t != null; t = t.next )
			for( i = t.nshifts-1; i >= 0; i-- ) {
				int newstate = t.shifts[i];
				symnum = state[newstate].symbol;
				e = goto_nshifts[symnum]++;
				term_from[e] = (short)t.number;
				term_to[e] = (short)newstate;
			}

		ntgotos = term_goto[nterms];
		nterm_goto = term_goto;
		ngotos -= ntgotos;
		nterm_from = term_from + ntgotos;
		nterm_to = term_to + ntgotos;
		for( i = nterms; i <= nsyms; i++ )
			nterm_goto[i] -= ntgotos;
	}


	// returns the number of goto, which shifts from state by symbol
	int select_goto( int state, int symbol )
	{
		int min = nterm_goto[symbol], max = nterm_goto[symbol+1]-1;
		int i, e;

		while( min <= max ) {
			e = (min + max) >> 1;
			i = nterm_from[e];
			if( i == state )
				return e;
			else if( i < state )
				min = e + 1;
			else
				max = e - 1;
		}

		assert false;
		return 0;
	}


	// add goto number to rule lookback list
	void add_lookback( int state, int rule, short gotono )
	{
		int i = laindex[state], max = laindex[state+1];

		for( ; i < max; i++ ) {
			if( larule[i] == rule ) {
				Short s = new Short();
				s.value = gotono;
				s.next = lookback[i];
				lookback[i] = s;
				return;
			}
		}

		assert false;
	}


	// fills: follow
	void init_follow()
	{
		int settrav = 0, nedges = 0;
		short[][] empties = graph;
		int e, k, i;

		follow = new int[ ngotos * termset ];
		Arrays.fill(follow, 0);

		for( i = 0; i < ngotos; i++, settrav += termset ) {
			int st = nterm_to[i];
			short[] shifts = state[st].shifts;
			int nshifts = state[st].nshifts, shifts_ind = 0;

			for( e = 0; e < nshifts && state[shifts[shifts_ind]].symbol < nterms; e++, shifts_ind++ )
				follow[settrav + (state[shifts[shifts_ind]].symbol)/BITS] |= (1<<((state[shifts[shifts_ind]].symbol)%BITS));

			for( ; e < nshifts; e++, shifts_ind++ ) {
				k = state[shifts[shifts_ind]].symbol;
				if( sym[k].empty )
					edge[nedges++] = (short)select_goto( st, k );
			}

			if( nedges != 0 ) {
				empties[i] = new short[nedges+1];
				short[] target = empties[i]; 
				for( int q = 0; q < nedges; q++)
					target[q] = edge[q];
				empties[i][nedges] = -1;
				nedges = 0;
			} else {
				empties[i] = null;
			}
		}

		graph_closure( empties );
		Arrays.fill(empties, 0);
	}


	// reverts all edges in graph
	void transpose_graph()
	{
		int n = ngotos;
		short[] nedges = new short[n];
		short[] p;
		int i;

		// calculate new row sizes
		Arrays.fill(nedges, (short)0);
		for( i = 0; i < n; i++ ) {
			p = graph[i];
			if( p != null ) {
				for( int e = 0; p[e] >= 0; e++ )
					nedges[p[e]]++;
			}
		}

		// allocate new graph
		short[][] newgraph = new short[n][];
		for( i = 0; i < n; i++ )
			newgraph[i] = (nedges[i] != 0) ? new short[nedges[i]+1] : null;

		// fill new graph
		Arrays.fill(nedges, (short)0);
		for( i = 0; i < n; i++ ) {
			p = graph[i];
			if( p != null ) {
				for( int e = 0; p[e] >= 0; e++ )
					newgraph[p[e]][nedges[p[e]]++] = (short)i;
			}
		}

		// insert -1 (end-of-row markers)
		for( i = 0; i < n; i++ ) {
			if( newgraph[i] != null )
				newgraph[i][nedges[i]] = -1;
		}

		graph = newgraph;
	}


//	 builds follow set
	void build_follow()
	{
		int i, e, length, currstate, nedges = 0, rpart;
		short[] states = new short[maxrpart+1];
		int[] rule;

		for( i = 0; i < ngotos; i++ ) {
			int fstate = nterm_from[i];
			int symbol = state[nterm_to[i]].symbol;

			rule = derives[symbol];
			for( int ruleind = 0; rule[ruleind] >= 0; ruleind++ ) {
				currstate = states[0] = (short)fstate;
				length = 1;

				for( rpart = rindex[rule[ruleind]]; rright[rpart] >= 0; rpart++ ) {
					int nshifts = state[currstate].nshifts;
					short[] shft = state[currstate].shifts;

					for( e = 0; e < nshifts; e++ ) {
						currstate = shft[e];
						if( state[currstate].symbol == rright[rpart] ) {
							break;
						}
					}
					assert e < nshifts;
					states[length++] = (short)currstate;
				}

				if( !state[currstate].LR0 )
					add_lookback( currstate, rule[ruleind], (short)i );

				for( length--;;) {
					rpart--;
					if( rpart >= 0 && rright[rpart] >= (int)nterms ) {
						currstate = states[--length];
						edge[nedges++] = (short)select_goto( currstate, rright[rpart] );
						if( sym[rright[rpart]].empty ) {
							continue;
						}
					}
					break;
				}
			}

			if( nedges != 0 ) {
				graph[i] = new short[nedges+1];
				short[] target = graph[i]; 
				for( int q = 0; q < nedges; q++)
					target[q] = edge[q];
				graph[i][nedges] = -1;
				nedges = 0;
			} else {
				graph[i] = null;
			}
		}

		show_graph();
		transpose_graph();
		show_graph();

		graph_closure( graph );

//		for( i = 0; i < ngotos; i++ )
//			delete[] graph[i];
//
//		delete[] states;
	}


	// fills: LA
	void buildLA() {
		final int n = laindex[nstates];
		Short s;
		int p, i, from;

		for (p = 0, i = 0; i < n; i++, p += termset) {

			// add all associated gotos
			for (s = lookback[i]; s != null; s = s.next) {
				from = termset * s.value;
				for (int q = 0; q < termset; q++)
					LA[p + q] |= follow[from + q];
			}

			// free lookback
			lookback[i] = null;
		}
	}


	// frees temporary data
	void freeLA() {
		lookback = null;
		follow = null;
	}

	// debug
	void show_follow() {
		if (debuglev < 2)
			return;

		int i, e;

		err.error(2, "\nFollow:\n");
		for (i = 0; i < ngotos; i++) {

			err.error(2, MessageFormat.format("{0,number,####} . {1,number,####}\t", nterm_from[i], nterm_to[i]));
			for (e = 0; e < nterms; e++)
				if (((follow[i*termset + (e) / BITS] & (1 << ((e) % BITS))) != 0))
					err.error(2, " " + sym[e].name);

			err.error(2, "\n");
		}
	}


	// debug
	void show_graph() {
		if (debuglev < 2)
			return;

		err.error(2, "\nGraph:\n");

		for (int i = 0; i < ngotos; i++) {
			short[] p = graph[i];
			if (p != null) {
				err.error(2, MessageFormat.format(" {0,number,####}: ", i));
				for (int e = 0; p[e] >= 0; e++)
					err.error(2, " " + p[e]);
				err.error(2, "\n");
			}
		}

	}


	// debug
	void show_lookaheads() {
		int i, e, k, max;

		if (debuglev == 0)
			return;

		err.error(2, "\nLookaheads:\n");

		for (i = 0; i < nstates; i++) {
			if (laindex[i] < laindex[i + 1]) {
				max = laindex[i + 1];
				err.error(2, i + ":\n");

				for (e = laindex[i]; e < max; e++) {
					k = rindex[larule[e]];
					while (rright[k] >= 0)
						k++;
					print_situation(2, k);
					int set = termset * e;

					err.error(2, "  >>>");
					for (k = 0; k < nterms; k++)
						if (((LA[set + (k) / BITS] & (1 << ((k) % BITS))) != 0))
							err.error(2, " " + sym[k].name);
					err.error(2, "\n");
				}
			}
		}

	}


	// graph closure // //////////////////////////////////////////////////////////////////////////////

	private int infinity, top;
	private short[][] relation;
	private short[] gc_index, gc_vertices;

	// process one vertex
	void do_vertex(int i) {
		int height, k;

		gc_vertices[++top] = (short) i;
		height = top;
		gc_index[i] = (short) top;

		if (relation[i] != null) {
			short[] row = relation[i];
			for (int e = 0; row[e] >= 0; e++) {
				if (gc_index[row[e]] == 0)
					do_vertex(row[e]);

				if (gc_index[i] > gc_index[row[e]])
					gc_index[i] = gc_index[row[e]];

				int from = termset * row[e], to = termset * i;
				for (k = 0; k < termset; k++)
					follow[to++] |= follow[from++];
			}
		}

		if (gc_index[i] == height)
			for (;;) {
				int e = gc_vertices[top--];
				gc_index[e] = (short) infinity;
				if (i == e)
					break;
				int from = termset * i, to = termset * e;
				for (k = 0; k < termset; k++)
					follow[to++] |= follow[from++];
			}
	}

	// modifies: follow (in according to digraph)
	void graph_closure(short[][] relation) {
		int i;

		this.relation = relation;
		gc_index = new short[ngotos];
		gc_vertices = new short[ngotos + 1];
		infinity = ngotos + 2;
		top = 0;

		for (i = 0; i < ngotos; i++)
			gc_index[i] = 0;

		for (i = 0; i < ngotos; i++)
			if (gc_index[i] == 0 && relation[i] != null)
				do_vertex(i);

		gc_index = null;
		gc_vertices = null;
	}

}
