/*   engine.cpp
 *
 *   Lapg (Lexical Analyzer and Parser Generator)
 *   Copyright (C) 2002-07  Evgeny Gryaznov (inspirer@inbox.ru)
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

#include "common.h"
#include "gbuild.h"

#define STATE_TABLE_SIZE 1037

// fills: state, nstates, firsts, ruleforvar, derives
int GrammarBuilder::LR0()
{
	allocate_data();
	build_derives();
	build_sets();
	initializeLR0();

	while( current ) {
		build_closure( current->elems );
		if( !process_state() ) {
			err->error( 0, "syntax analyzer is too big ...\n" );
			freeLR0();
			state = new State*[nstates];
			for( State *t = first; t; t = t->next )
				state[t->number] = t;
			return 0;
		}
		current = current->next;
	}

	add_final_states();
	show_debug();
	freeLR0();
	return 1;
}


// for each nonterm finds numbers of rules which can derive it
void GrammarBuilder::build_derives()
{
	int i, e;
	short *q, *l, *m;

	l = new short[ rules + nvars ];
	q = new short[ rules + nvars ];
	m = q + rules;
	for( i = 0; i < nvars; i++ ) m[i] = -1;

	for( i = rules - 1; i >= 0; i-- ) {
		e = rleft[i] - nterms;
		q[i] = m[e];
		m[e] = i;
	}

	derives = new short*[ nvars ];

	for( i = 0; i < nvars; i++ ) {
		derives[i] = l;
		for( e = m[i]; e!=-1; e = q[e] )
			*l++ = e;
		*l++ = -1;
	}

	derives -= nterms;
	delete[] q;
}


// fills: first, ruleforvar
void GrammarBuilder::build_sets()
{
	int i, e, j;
	short *p;
	
	// firsts
	firsts = new int[ nvars * varset ];
	memset( firsts, 0, nvars*varset*sizeof(int) );

	for( i = nterms; i < nsyms; i++ )
		for( p = derives[i]; *p >= 0; p++ ) {
			e = rright[rindex[*p]];
			if( e >= (int)nterms )
				SET( firsts + varset*(i-nterms), e - nterms );
		}

	// [j,i] && [i,e] => [j,e]
	for( i = 0; i < nvars; i++ )
		for( j = 0; j < nvars; j++ )
			if( IS( firsts + varset*j, i ) )
				for( e = 0; e < nvars; e++ )
					if( IS( firsts + varset*i, e ) )
						SET( firsts + varset*j, e );

	// set [i,i]
	for( i = 0; i < nvars; i++ )
		SET( firsts + varset*i, i );

	// ruleforvar
	ruleforvar = new int[ nvars * ruleset ];
	memset( ruleforvar, 0, nvars*ruleset*sizeof(int) );

	for( i = 0; i < nvars; i++ )
		for( e = 0; e < nvars; e++ )
			if( IS( firsts + varset*i, e ) )
				for( p = derives[e+nterms]; *p >= 0; p++ )
					SET( ruleforvar + ruleset*i, *p );

	ruleforvar -= nterms * ruleset;
	firsts -= nterms * varset;
}


// define all necessary values and allocate all temporary arrays for LR0
void GrammarBuilder::allocate_data()
{
	ruleset	= SIZE(rules);
	varset = SIZE(nvars);
	termset = SIZE(nterms);
	sitset = SIZE(situations);

	toreduce = new short[ rules + 1 ];
	closure = new short [ situations ];
	closurebit = new int[ ruleset ];
	
	table = new State*[STATE_TABLE_SIZE];
	memset( table, 0, STATE_TABLE_SIZE*sizeof(State*) );

	// state transition temporary data
	short *symnum = new short[nsyms];
	memset( symnum, 0, nsyms*sizeof(short) );

	int i, count;

	for( count = i = 0; i < situations; i++ )
		if( rright[i]>=0 ) 
			count++, symnum[rright[i]]++;

	short *items = new short[ count ];
	symbase = new short*[ nsyms ];
	symend = new short*[ nsyms ];

	for( count = i = 0; i < nsyms; i++ ) {
		symbase[i] = items + count;
		count += symnum[i];
	}
	symcanshift = symnum;
}


// create first state
void GrammarBuilder::initializeLR0()
{
	nstates = 1;
	first = last = current = new State;
	current->nreduce = current->nshifts = current->number = current->symbol = current->fromstate = 0;
	current->next = current->link = NULL;
	current->shifts = current->reduce = NULL;
	current->elems[0] = -1;
}


// fills: closure (closureend)
void GrammarBuilder::build_closure( short *prev )
{
	int e, rulebit, rule, index;
	int *q, *p, *end = closurebit+ruleset;
	short *l;

	p = closurebit;

	if( *prev == -1 ) {
		q = ruleforvar + input * ruleset;
		while( p < end ) *p++ = *q++;

	} else {
		while( p < end ) *p++ = 0;

		for( l = prev; *l >= 0; l++ ) {
			e = rright[*l];
			if( e >= (int)nterms ) {
				q = ruleforvar + e * ruleset;
				p = closurebit;
				while( p < end ) *p++ |= *q++;
			}
		}
	}

	rule = 0;
	closureend = closure;
	p = closurebit;
	l = prev;
	while( p < end ) {
		rulebit = *p++;
		if( !rulebit ) rule += BITS;
		else for( e = 0; e < BITS; e++ ) {
			if( rulebit & (1<<e) ) {
				index = rindex[rule];
				while( *l >= 0 && *l < index )
					*closureend++ = *l++;
				*closureend++ = index;
			}
			rule++;
		}
	}

	while( *l >= 0 )
		*closureend++ = *l++;
}


// create new state
GrammarBuilder::State *GrammarBuilder::new_state( int from, int by, unsigned hash, int size )
{
	last = last->next = (State *)new char[sizeof(State) + size*sizeof(short)];
	last->link = table[ hash%STATE_TABLE_SIZE ];
	table[ hash%STATE_TABLE_SIZE ] = last;
	last->fromstate = from;
	last->symbol = by;
	last->number = nstates++;
	last->nshifts = last->nreduce = 0;
	last->next = NULL;
	last->reduce = last->shifts = NULL;
	last->LR0 = 1;
	last->elems[size] = -1;
	return last;
}


// returns the state number is achieved having the 'symbol'
short GrammarBuilder::goto_state( int symbol )
{
	short *new_core = symbase[symbol];
	int size = symend[symbol] - new_core;
	unsigned i, hash;
	short *p, *c;
	State *t;

	for( hash = i = 0; i < size; i++ ) hash += new_core[i];
	t = table[ hash%STATE_TABLE_SIZE ];

	while( t ) {
		for( c = new_core, p = t->elems, i = 0; i < size; i++ )
			if( *c++ != *p++ )
				break;

		if( i == size )
			break;

		t = t->link;
	}

	if( !t ) {
		t = new_state( current->number, symbol, hash, size );
		for( c = new_core, p = t->elems, i = 0; i < size; i++ ) *p++ = *c++;
		*p++ = -1;
	}

	return t->number;
}


// fills: current->(n)shifts/(n)reduce
int GrammarBuilder::process_state()
{
	int i, sym;
	short *c, *e;

	ntoreduce = 0;
	for( i = 0; i < nsyms; i++ )
		symend[i] = NULL;

	for( c = closure; c < closureend; c++ ) {
		sym = rright[*c];
		if( sym >= 0 ) {
			e = symend[sym];
			if( !e ) e = symbase[sym];
			*e++ = *c + 1;
			symend[sym] = e;

		} else 
			toreduce[ntoreduce++] = -1-rright[*c];
	}

	ntoshift = 0;
	c = symcanshift;
	for( i = 0; i < nsyms; i++ )
		if( symend[i] )
			ntoshift++, *c++ = i;

	current->nshifts = ntoshift;
	current->nreduce = ntoreduce;
	if( ntoshift + ntoreduce ) 
		current->shifts = new short[ ntoshift + ntoreduce ];
	else
		current->shifts = NULL;
	current->reduce = current->shifts + ntoshift;

	for( i = 0; i < ntoshift; i++ ) {
		current->shifts[i] = goto_state( symcanshift[i] );
		if( current->shifts[i] >= MAX_WORD ) return 0;
	}

	for( i = 0; i < ntoreduce; i++ )
		current->reduce[i] = toreduce[i];

	current->LR0 = (ntoreduce > 1 || ntoshift && ntoreduce )? 0 : 1;
	return 1;
}


// prints the 'situation' to 'errl' output stream
void GrammarBuilder::print_situation( int errl, int situation )
{
	int *rr = rright + situation, rulenum, i;

	// left part of the rule
	while( *rr>=0 ) rr++;
	rulenum = -*rr-1;
	err->error( errl, "  %s ::=", sym[rleft[rulenum]].name );

	i = rindex[rulenum];
	for( rr = rright + i; *rr>=0; rr++, i++ ) {
		if( i == situation ) err->error( errl, " _" );
		err->error( errl, " %s", sym[*rr].name );
	}
	if( i == situation ) err->error( errl, " _" );
	err->error( errl, "\n" );
}


// inserts shift from t to tostate
void GrammarBuilder::insert_shift( State *t, int tostate )
{
	if( t->shifts ) {
		const int symbol = state[tostate]->symbol;
		short *p, *old, *f = t->shifts;
		int i, n = t->nshifts + t->nreduce;

		old = f;
		t->shifts = p = new short[n+1];
		for( i = 0; i < t->nshifts && state[*f]->symbol < symbol; i++ )
			*p++ = *f++;
		*p++ = tostate;
		for(; i < n; i++ ) *p++ = *f++;
		t->nshifts++;
		t->reduce = t->shifts + t->nshifts;
		delete[] old;

	} else {
		t->nshifts = 1;
		t->shifts = new short[1];
		t->reduce = t->shifts + 1;
		t->shifts[0] = tostate;
	}			 
}


// fills: state, creates: next_to_final, final
void GrammarBuilder::add_final_states()
{
	State *next_to_final, *final, *t;
	int created = 0;

	// search for next_to_final
	for( next_to_final = first->next;
	     next_to_final && next_to_final->fromstate == 0;
		 next_to_final = next_to_final->next )
			if( next_to_final->symbol == input ) break;

	if( next_to_final && next_to_final->fromstate!=0 ) 
		next_to_final = NULL;

	// if not found then create
	if( !next_to_final ) {
		next_to_final = new_state( 0, input, 0, 0 );
		created = 1;
	}

	final = new_state( next_to_final->number, eoi, 0, 0 );

	// create state array
	state = new State*[nstates];
	for( t = first; t; t = t->next )
		state[t->number] = t;

	// insert shifts
	if( created )
		insert_shift( first, next_to_final->number );
	insert_shift( next_to_final, final->number );
}


// frees temporary data
void GrammarBuilder::freeLR0()
{
	delete[] toreduce;
	delete[] closure;
	delete[] closurebit;
	delete[] table;
	delete[] symbase[0];
	delete[] symbase;
	delete[] symend;
	delete[] symcanshift;
}


// show the necessary debug info
void GrammarBuilder::show_debug()
{
	short *p;
	State *t;

	if( debuglev ) {
		err->error( 2, "\nStates\n0:\n" );

		for( t = first; t; t = t->next ) {
			if( t != first )
				err->error( 2, "\n%i: (from %i, %s)\n", 
								t->number, t->fromstate, sym[t->symbol].name );

			build_closure( t->elems );

			for( p = closure; p < closureend; p++ )
				print_situation( 2, *p );
		}
	}
}


// lalr ///////////////////////////////////////////////////////////////////////////////////////


// resolving lr0 conflicts using lalr
void GrammarBuilder::lalr()
{
	initializeLA();
	init_goto();

	edge = new short[ngotos+1];
	graph = new short*[ngotos];

	init_follow();
	build_follow();
	show_follow();

	buildLA();
	show_lookaheads();

	delete[] edge;
	delete[] graph;

	freeLA();
	
	for( int i = nterms; i <= nsyms; i++ )
		nterm_goto[i] += ntgotos;
}


// fills: laindex, larules, maxrpart; creates: lookback, LA
void GrammarBuilder::initializeLA()
{
	int i, e, k;
	short *la;
	State *t;

	// calculate maxrpart
	maxrpart = 2;
	for( e = i = 0; i < situations; i++ ) {
		if( rright[i] < 0 ) {
			if( e > maxrpart ) maxrpart = e;
			e = 0;
		} else e++;
	}

	// get larule size
	for( e = 0, t = first; t; t = t->next )
		if( !t->LR0 )
			e += t->nreduce;

	if( !e ) e = 1;

	// allocate
	laindex = new short[ nstates+1 ];
	laindex[nstates] = e;
	la = larule = new short[ e ];
	lookback = new Short*[ e ];
	LA = new int[ e * termset ];

	memset( lookback, 0, e*sizeof(short*) );
	memset( LA, 0, e*termset*sizeof(int) );

	// fills: larule, laindex
	for( i = 0, t = first; t; t = t->next ) {
		laindex[t->number] = i;
		if( !t->LR0 ) {
			for( k = 0; k < t->nreduce; k++ )
				*la++ = t->reduce[k];
			i += t->nreduce;
		}
	}
}


// fills: nterm_goto, nterm_from, nterm_to
void GrammarBuilder::init_goto()
{
	State *t;
	int i, e, symnum;
	short *goto_nshifts;

	goto_nshifts = new short[ nsyms ];
	term_goto = new short[ nsyms + 1 ];
	memset( goto_nshifts, 0, nsyms * sizeof(short) );

	ngotos = 0;
	for( t = first; t; t = t->next )
		for( i = t->nshifts-1; i >= 0; i-- ) {
			symnum = state[t->shifts[i]]->symbol;
			goto_nshifts[symnum]++;
			ngotos++;
		}

	for( e = i = 0; i < nsyms; i++ ) {
		term_goto[i] = e;
		e += goto_nshifts[i];
		goto_nshifts[i] = term_goto[i];
	}
	term_goto[nsyms] = ngotos;
	term_from = new short[ ngotos ];
	term_to = new short[ ngotos ];

	for( t = first; t; t = t->next )
		for( i = t->nshifts-1; i >= 0; i-- ) {
			int newstate = t->shifts[i];
			symnum = state[newstate]->symbol;
			e = goto_nshifts[symnum]++;
			term_from[e] = t->number;
			term_to[e] = newstate;
		}

	ntgotos = term_goto[nterms];
	nterm_goto = term_goto;
	ngotos -= ntgotos;
	nterm_from = term_from + ntgotos;
	nterm_to = term_to + ntgotos;
	for( i = nterms; i <= nsyms; i++ )
		nterm_goto[i] -= ntgotos;

	delete[] goto_nshifts;
}


// returns the number of goto, which shifts from state by symbol
int GrammarBuilder::select_goto( int state, int symbol )
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

	ASSERT(0);
	return 0;
}


// add goto number to rule lookback list
void GrammarBuilder::add_lookback( int state, int rule, short gotono )
{
	int i = laindex[state], max = laindex[state+1];

	for( ; i < max; i++ ) if( larule[i] == rule ) {

		Short *s = new Short;
		s->value = gotono;
		s->next = lookback[i];
		lookback[i] = s;
		return;
	}

	ASSERT(0);
}


// fills: follow
void GrammarBuilder::init_follow()
{
	int *settrav, nedges = 0;
	short **empties = graph;
	int e, k, i;

	settrav = follow = new int[ ngotos * termset ];
	memset( follow, 0, ngotos*termset*sizeof(int) );

	for( i = 0; i < ngotos; i++, settrav += termset ) {
		int st = nterm_to[i];
		short *shifts = state[st]->shifts;
		int nshifts = state[st]->nshifts;

		for( e = 0; e < nshifts && state[*shifts]->symbol < nterms; e++, shifts++ )
			SET( settrav, state[*shifts]->symbol );
		
		for( ; e < nshifts; e++, shifts++ ) {
			k = state[*shifts]->symbol;
			if( sym[k].empty )
				edge[nedges++] = select_goto( st, k );
		}

		if( nedges ) {
			empties[i] = new short[nedges+1];
			memcpy( empties[i], edge, nedges*sizeof(short) );
			empties[i][nedges] = -1;
			nedges = 0;
		} else empties[i] = NULL;
	}

	graph_closure( empties );

	for( i = 0; i < ngotos; i++ )
		delete[] empties[i];
}


// reverts all edges in graph
void GrammarBuilder::transpose_graph()
{
	int n = ngotos;
	short *p, *nedges = new short[n];
	int i;

	// calculate new row sizes
	memset( nedges, 0, n*sizeof(short) );
	for( i = 0; i < n; i++ )
		if( (p = graph[i]) )
			while( *p >= 0 )
				nedges[*p++]++;

	// allocate new graph
	short **newgraph = new short*[n];
	for( i = 0; i < n; i++ )
		newgraph[i] = (nedges[i])?new short[nedges[i]+1]:NULL;

	// fill new graph
	memset( nedges, 0, n*sizeof(short) );
	for( i = 0; i < n; i++ )
		if( (p = graph[i]) )
			for( ; *p >= 0; p++)
				newgraph[*p][nedges[*p]++] = i;

	// insert -1 (end-of-row markers)
	for( i = 0; i < n; i++ )
		if( newgraph[i] )
			newgraph[i][nedges[i]] = -1;

	// free old graph
	for( i = 0; i < n; i++ )
		delete[] graph[i];
	delete[] graph;

	graph = newgraph;

	delete[] nedges;
}


// builds follow set
void GrammarBuilder::build_follow()
{
	int i, e, length, currstate, nedges = 0, *rpart;
	short *states = new short[maxrpart+1];
	short *rule;

	for( i = 0; i < ngotos; i++ ) {
		int fstate = nterm_from[i];
		int symbol = state[nterm_to[i]]->symbol;

		for( rule = derives[symbol]; *rule >= 0; rule++ ) {
			currstate = states[0] = fstate;
			length = 1;

			for( rpart = rright + rindex[*rule]; *rpart >= 0; rpart++ ) {
				int nshifts = state[currstate]->nshifts;
				short *shft = state[currstate]->shifts;

				for( e = 0; e < nshifts; e++ ) {
					currstate = shft[e];
					if( state[currstate]->symbol == *rpart ) break;
				}
				ASSERT( e < nshifts );
				states[length++] = currstate;
			}

			if( !state[currstate]->LR0 )
				add_lookback( currstate, *rule, i );

			for( length--;;) {
				rpart--;
				if( rpart>=rright && *rpart>=(int)nterms ) {
					currstate = states[--length];
					edge[nedges++] = select_goto( currstate, *rpart );
					if( sym[*rpart].empty ) continue;
				}
				break;
			}
		}

		if( nedges ) {
			graph[i] = new short[nedges+1];
			memcpy( graph[i], edge, nedges*sizeof(short) );
			graph[i][nedges] = -1;
			nedges = 0;
		} else graph[i] = NULL;
	}

	show_graph();
	transpose_graph();
	show_graph();

	graph_closure( graph );

	for( i = 0; i < ngotos; i++ )
		delete[] graph[i];

	delete[] states;
}


// fills: LA
void GrammarBuilder::buildLA()
{
	const int n = laindex[nstates];
	Short *s, *t;
	int *p, i, *to, *from, *end;
	
	for( p = LA, i = 0; i < n; i++, p += termset ) {
		end = p + termset;

		// add all associated gotos
		for( s = lookback[i]; s; s = s->next ) {
			to = p;
			from = follow + termset * s->value;
			while( to < end ) *to++ |= *from++;
		}

		// free lookback
		for( s = lookback[i]; s; s = t ) {
			t = s->next;
			delete s;
		}
	}
}


// frees temporary data
void GrammarBuilder::freeLA()
{
	delete[] lookback;
	delete[] follow;
}

// debug
void GrammarBuilder::show_follow()
{
	int i, e, *row;

	if( debuglev >= 2 ) {
		err->error( 2, "\nFollow:\n" );
		for( i = 0, row = follow; i < ngotos; i++, row += termset ) {
			
			err->error( 2, "%4i -> %-4i\t", nterm_from[i], nterm_to[i] );
			for( e = 0; e < nterms; e++ )
				if( IS(row,e) )
					err->error( 2, " %s", sym[e].name );

			err->error( 2, "\n" );
		}
	}
}


// debug
void GrammarBuilder::show_graph()
{
	short i, *p;

	if( debuglev >= 2 ) {
		err->error( 2, "\nGraph:\n" );

		for( i = 0; i < ngotos; i++ )
			if( graph[i] ) {
				err->error( 2, " %4i: ", i );
				for( p = graph[i]; *p >= 0; p++ )
					err->error( 2, " %i", *p );
				err->error( 2, "\n" );
			}
	}
}


// debug
void GrammarBuilder::show_lookaheads()
{
	int i, e, k, max, *set;

	if( debuglev ) {
		err->error( 2, "\nLookaheads:\n" );

		for( i = 0; i < nstates; i++ ) 
			if( laindex[i] < laindex[i+1] ) {
				max = laindex[i+1];
				err->error( 2, "%i:\n", i );

				for( e = laindex[i]; e < max; e++ ) {
					k = rindex[larule[e]];
					while( rright[k] >= 0 ) k++;
					print_situation( 2, k );
					set = LA + termset * e;
					
					err->error( 2, "  >>>" );
					for( k = 0; k < nterms; k++ )
						if( IS( set, k ) )
							err->error( 2, " %s", sym[k].name );
					err->error( 2, "\n" );
				}
			}
	}

}


// graph closure //////////////////////////////////////////////////////////////////////////////


// process one vertex
void GrammarBuilder::do_vertex( int i )
{
	int height, e, k, *from, *to;
	short *row;

	gc_vertices[++top] = i;
	gc_index[i] = height = top;

	if( relation[i] )
		for( row = relation[i]; *row >= 0; row++ ) {
			if( !gc_index[*row] )
				do_vertex( *row );

			if( gc_index[i] > gc_index[*row] )
				gc_index[i] = gc_index[*row];

			for( from = follow + termset*row[0], to = follow + termset*i, k = 0; k < termset; k++ )
				*to++ |= *from++;
		}

	if( gc_index[i] == height )
		for(;;) {
			e = gc_vertices[top--];
			gc_index[e] = infinity;
			if( i == e ) break;
			for( from = follow + termset*i, to = follow + termset*e, k = 0; k < termset; k++ )
				*to++ = *from++;
		}
}


// modifies: follow (in according to digraph)
void GrammarBuilder::graph_closure( short **relation )
{
	int i;

	GrammarBuilder::relation = relation;
	gc_index = new short[ngotos];
	gc_vertices = new short[ngotos+1];
	infinity = ngotos + 2;
	top = 0;

	for( i = 0; i < ngotos; i++ )
		gc_index[i] = 0;

	for( i = 0; i < ngotos; i++ )
		if( !gc_index[i] && relation[i] )
			do_vertex(i);

	delete[] gc_index;
	delete[] gc_vertices;
}
