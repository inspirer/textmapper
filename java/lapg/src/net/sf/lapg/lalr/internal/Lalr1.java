package net.sf.lapg.lalr.internal;

import java.text.MessageFormat;
import java.util.Arrays;

import net.sf.lapg.lalr.Grammar;
import net.sf.lapg.lalr.IError;

class Lalr1 extends LR0 {
	
	public class Short {
		short value;
		Short next;
	};

	Lalr1(Grammar g, IError err, int debuglev) {
		super(g, err, debuglev);
	}

	// LALR

	protected short[] larule, laindex;
	protected int[] LA /* ? -> setof(term) */;
	
	private int maxrpart, ngotos, ntgotos;
	private Short[] lookback;
	private short[] nterm_goto, nterm_from, nterm_to;
	private short[] edge;
	private short[][] graph;
	private int[] follow /* ngotos ->setof(term) */;

	protected void buildLalr() {
		LA = null;
		laindex = larule = null;

		initializeLA();
		init_goto();

		edge = new short[ngotos + 1];
		graph = new short[ngotos][];

		init_follow();
		build_follow();
		show_follow();

		buildLA();
		show_lookaheads();

		edge = null;
		graph = null;

		freeLA();

		for (int i = nterms; i <= nsyms; i++)
			nterm_goto[i] += ntgotos;
	}


// fills: laindex, larules, maxrpart; creates: lookback, LA
	private void initializeLA()
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
	private void init_goto()
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
		
// ===================== TODO TODO
//		nterm_from = term_from + ntgotos;
//		nterm_to = term_to + ntgotos;
		for( i = nterms; i <= nsyms; i++ )
			nterm_goto[i] -= ntgotos;
	}


	// returns the number of goto, which shifts from state by symbol
	private int select_goto( int state, int symbol )
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
	private void add_lookback( int state, int rule, short gotono )
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
	private void init_follow()
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
	private void transpose_graph()
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
	private void build_follow()
	{
		int i, e, length, currstate, nedges = 0, rpart;
		short[] states = new short[maxrpart+1];
		int[] rule;

		for( i = 0; i < ngotos; i++ ) {
			int fstate = nterm_from[i];
			int symbol = state[nterm_to[i]].symbol;

			rule = derives[symbol-nterms];
			for( int ri = 0; ri < rule.length; ri++ ) {
				currstate = states[0] = (short)fstate;
				length = 1;

				for( rpart = rindex[rule[ri]]; rright[rpart] >= 0; rpart++ ) {
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
					add_lookback( currstate, rule[ri], (short)i );

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
	private void buildLA() {
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
	private void freeLA() {
		lookback = null;
		follow = null;
	}

	// debug
	private void show_follow() {
		if (debuglev < 2)
			return;

		int i, e;

		err.debug( "\nFollow:\n");
		for (i = 0; i < ngotos; i++) {

			err.debug( MessageFormat.format("{0,number,####} . {1,number,####}\t", nterm_from[i], nterm_to[i]));
			for (e = 0; e < nterms; e++)
				if (((follow[i*termset + (e) / BITS] & (1 << ((e) % BITS))) != 0))
					err.debug( " " + sym[e].name);

			err.debug( "\n");
		}
	}


	// debug
	private void show_graph() {
		if (debuglev < 2)
			return;

		err.debug( "\nGraph:\n");

		for (int i = 0; i < ngotos; i++) {
			short[] p = graph[i];
			if (p != null) {
				err.debug( MessageFormat.format(" {0,number,####}: ", i));
				for (int e = 0; p[e] >= 0; e++)
					err.debug( " " + p[e]);
				err.debug( "\n");
			}
		}

	}


	// debug
	private void show_lookaheads() {
		int i, e, k, max;

		if (debuglev == 0)
			return;

		err.debug( "\nLookaheads:\n");

		for (i = 0; i < nstates; i++) {
			if (laindex[i] < laindex[i + 1]) {
				max = laindex[i + 1];
				err.debug( i + ":\n");

				for (e = laindex[i]; e < max; e++) {
					k = rindex[larule[e]];
					while (rright[k] >= 0)
						k++;
					print_situation(k);
					int set = termset * e;

					err.debug( "  >>>");
					for (k = 0; k < nterms; k++)
						if (((LA[set + (k) / BITS] & (1 << ((k) % BITS))) != 0))
							err.debug( " " + sym[k].name);
					err.debug( "\n");
				}
			}
		}

	}


	// graph closure // //////////////////////////////////////////////////////////////////////////////

	private int infinity, top;
	private short[][] relation;
	private short[] gc_index, gc_vertices;

	// process one vertex
	private void do_vertex(int i) {
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
	private void graph_closure(short[][] relation) {
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
