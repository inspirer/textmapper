/*   gbuild.cpp
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


//	symbol - saves symbol for subsequent using and returns its number
int GrammarBuilder::symbol( char *sname, int def_type, char *type, int sibling )
{
	unsigned i, e;
	Symbol s;

	// search's for a symbol in the table
	for( i = 0; i < nsyms; i++ ) {
		if( !sym[i].is_attr && !strcmp( sname, sym[i].name ) )
			break;
	}

	// symbol have been found
	if( i < nsyms ) {
		switch( def_type ) {
		case 0:
			if( sibling != -2 ) {
				if( sym[i].has_attr )
					err->error( 0, "L-attribute for symbol `%s' already defined\n", sym[i].name );
				else 
					sym[i].has_attr = 1, sym[i].sibling = sibling;
			}
			return i;
		case 1:
			if( sym[i].term ) {
				err->error( 0, "error: terminal used as the left part of rule: %s\n", sname );
				errors++;
			} else {
				sym[i].defed = 1;
				if( type && !sym[i].type ) sym[i].type = _strdup( type );
			}
			return i;
		case 2:
			sname = "{}";
			break;
		}
	}

	// initialize structure
	s.empty = s.term = s.good = s.employed = s.temp = s.has_attr = s.is_attr = 0;
	s.name = _strdup( sname );
	s.defed = (def_type) ? 1 : 0;
	s.opt = -1;
	s.sibling = sibling;
	s.type = _strdup(type);

	if( def_type == 0 && sibling != -2 )
		s.has_attr = 1;

	if( def_type == 2 ) {
		ASSERT( i < nsyms );
		s.is_attr = 1;
	}

	// add's symbol
	inc_realloc( (void**)&sym, nsyms, sizeof(Symbol) );
	sym[nsyms] = s;

	// check for *opt symbol
	e = nsyms++;
	i = strlen(sname)-3;
	if( def_type != 2 && i > 0 && sname[i]=='o' && sname[i+1]=='p' && sname[i+2]=='t' ) {

		if( def_type == 1 ) {
			err->error( 0, "error: defined symbol with opt at end: `%s'\n", sname );
			errors++;
		}

		sname[i] = 0;
		int k = symbol( sname, 0 );
		sym[k].opt = e;
		sname[i] = 'o';

		int one_rule[] = { e, k };
		rule( 1, 0, 0, one_rule, -1 );
		rule( 0, 0, 0, one_rule, -1 );
		sym[e].defed = 1;
	}
	return e;
}


//	terminal - like symbol, but for tokens
int GrammarBuilder::terminal( char *name, char *type )
{
	int number;

	ASSERT( nsyms==nterms );

	if( !strcmp( name, "input" ) ) {
		err->error( 0, "error: wrong name for terminal: %s\n", name );
		errors++;
		number = 0;
	} else {
		number = symbol( name, 0, type );
		sym[number].term = 1;
		nterms = nsyms;

		if( !strcmp( name, "error" ) )
			errorn = number;
	}
	return number;
	}


void GrammarBuilder::addprio( char *id, int prio, int restofgroup )
{
	if( prio ) {
		int s = symbol( id, 0 );
		if( !restofgroup ) {
			inc_realloc( (void**)&priorul, nprio, sizeof(int) );
			priorul[nprio++] = -prio;
		}
		inc_realloc( (void**)&priorul, nprio, sizeof(int) );
		priorul[nprio++] = s;
	}
	delete[] id;
}

//	set_input - define the main symbol
void GrammarBuilder::set_input( int i )
{
	ASSERT( input==-1 && i>=(int)nterms && i<(int)nsyms );
	input = i;
}


//	set_eoi - define the end_of_input symbol
void GrammarBuilder::set_eoi( int i )
{
	ASSERT( eoi==-1 && i<(int)nterms && i>=0 );
	eoi = i;
}


//	rule - saves the rule for processing
void GrammarBuilder::rule( unsigned length, int priority, char *action, int *array, int defline )
{
	unsigned rpos, i;
	int *rr, *right = array+1;

	// calculate priority if needed
	if( priority == -1 ) {
		for( i = length; i > 0; i-- )
			if( sym[array[i]].term ) {
				priority = array[i];
				break;
			}
	}

	// resize arrays
	inc_realloc( (void**)&rleft, rules, sizeof(int) );
	inc_realloc( (void**)&rindex, rules, sizeof(int) );
	inc_realloc( (void**)&rlines, rules, sizeof(int) );
	inc_realloc( (void**)&rprio, rules, sizeof(int) );
	inc_realloc( (void**)&raction, rules, sizeof(char*) );
	rpos = x_realloc( (void**)&rright, right_used, situations, (length+1), sizeof(int) );
	if( !length ) sym[*array].empty = 1;
	rr = rright + rpos;

	rleft[rules] = *array;
	rindex[rules] = rpos;
	rlines[rules] = defline;
	rprio[rules] = priority;
	for( i = 0; i < length; i++ ) {
		if( sym[*right].is_attr ){
			sym[*right].length = i;
			sym[*right].rpos = rpos;

		} else if( sym[*right].has_attr ) {
			if( !i && *right!=*array // we allow left-recursive rules
			 || i && sym[*right].sibling == -1 && !sym[right[-1]].is_attr
			 || i && sym[*right].sibling >= 0  && right[-1] != sym[*right].sibling )
				err->error( 0, "L-attribute for symbol `%s' is omitted\n", 
					sym[*right].name );
		}
		*rr++ = *right++;
	}

	*rr++ = -1-rules;
	raction[rules] = action;
	rpos = rules++;
}


//	grammar_fixup - performs pre-generate fixups
void GrammarBuilder::fix_grammar()
{
	unsigned i, e;

	// fix types for *opt
	for( i = 0; i < nsyms; i++ ) {
		if( sym[i].opt != -1 ) {
			e = sym[i].opt;
			sym[e].type = _strdup( sym[i].type );
		}
		if( sym[i].is_attr )
			sym[i].type = _strdup( sym[sym[i].sibling].type );
	}
}


//	verify_grammar - prints useless symbols
void GrammarBuilder::verify_grammar()
{
	unsigned i, k, h;
	int *l;

	// search for symbols which accepts the empty chain
	search_next_empty:
	for( i = 0; i < rules; i++ ) if( !sym[rleft[i]].empty ) {

		k = 1;
		for( l = rright + rindex[i]; k && (*l >= 0); l++)
			if( !sym[*l].empty ) 
				k = 0;

		if( k ) {
			sym[rleft[i]].empty = 1;
			goto search_next_empty;
		}
	}

	// terminal and empty symbols are good
	for( i = 0; i < nsyms; i++ ) 
		if( sym[i].term || sym[i].empty ) 
			sym[i].good = 1;

	// search for the good symbols
	get_next_good:
	for( i = 0; i < rules; i++ ) if( !sym[rleft[i]].good ) {

		k = 1;
		for( l = rright + rindex[i]; k && (*l >= 0); l++)
			if( !sym[*l].good ) 
				k = 0;

		if( k ) {
			sym[rleft[i]].good = 1;
			goto get_next_good;
		}
	}

	// search for the employed symbols
	k = 1;
	sym[input].temp = 1;
	while( k ) {
		k = 0;
		for( i = 0; i < nsyms; i++ ) if( sym[i].temp ) {

			for( h = 0; h < rules; h++ ) if( rleft[h] == (int)i )
				for( l = rright + rindex[h]; *l >= 0; l++) {

					if( !sym[*l].temp && !sym[*l].employed ) {
						if( sym[*l].term )
							sym[*l].employed = 1;
						else
							k = 1, sym[*l].temp = 1;
					}
				}

			sym[i].employed = 1;
			sym[i].temp = 0;
		}
	}

	// eoi is very useful token
	sym[eoi].good = sym[eoi].employed = 1;

	// print out the useless symbols
	for( i = 0; i < nsyms; i++ ) 
		if( !sym[i].term && !sym[i].defed )
			err->error( 0, "no rules for `%s`\n", sym[i].name );
		else if( !sym[i].good || !sym[i].employed )
			if( strncmp( sym[i].name, "_skip", 5 ) )
				err->error( 1, "lapg: symbol `%s` is useless\n", sym[i].name );
}


// prints the given rule to 'errl' output stream
void GrammarBuilder::print_rule( int errl, int rule )
{
	int *rr = rright + rindex[rule];

	err->error( errl, "  %s ::=", sym[rleft[rule]].name );

	for( ; *rr>=0; rr++ )
		err->error( errl, " %s", sym[*rr].name );

	err->error( errl, "\n" );
}

void GrammarBuilder::print_input( int s )
{
	if( state[s]->number == 0 ) return;
	print_input(state[s]->fromstate);
	err->error( 1, " %s", sym[state[s]->symbol].name );
}


//  returns 0:unresolved 1:shift 2:reduce
int GrammarBuilder::compare_prio( int rule, int next )
{
	int i, cgroup, assoc, rule_group = -1, next_group = -1, nextassoc = -1;
	
	if( !nprio )
		return 0;
	
	for( cgroup = i = 0; i < nprio; i++ )
		if( priorul[i] < 0 ) {
			assoc = -priorul[i];
			cgroup++;
		} else {
			if( priorul[i] == rprio[rule] )
				rule_group = cgroup;			
			if( priorul[i] == next ) {
				next_group = cgroup;
				nextassoc = assoc;
			}
		}
	
	if( rule_group == -1 || next_group == -1 )
		return 0;
	if( rule_group > next_group )
		return 2;               // reduce
	if( rule_group < next_group )
		return 1;               // shift
	if( nextassoc == 1 )
		return 2;               // left => reduce
	if( nextassoc == 2 )
		return 1;               // right => shift
	return 0;
}


// fills: action_index, action_table, nactions
void GrammarBuilder::action()
{
	State *t;
	int rr=0, sr=0, conflict, n=0;
	short *actionset, *next, setsize;
	short *p, *f;
	int  i, *end, *la, sm;

	next = new short[nterms];
	actionset = new short[nterms];
	action_index = new int[nstates];
	action_table = NULL;
	nactions = nactions_used = 0;

	for( t = first; t; t = t->next ) {
		if( t->LR0 ) {
			if( t->nshifts ) {
				action_index[t->number] = -1;
			} else if( t->nreduce ) {
				action_index[t->number] = *t->reduce;
			} else 
				action_index[t->number] = -2;
		} else {

			// prepare
			setsize = conflict = 0;
			for( i = 0; i < nterms; i++ )
				next[i] = -2;
				
			// process shifts
			for( p = t->shifts, i = 0; i < t->nshifts; i++, p++ ) {
				sm = state[*p]->symbol;
				if( sm >= nterms ) break;
				ASSERT(next[sm]==-2);
				next[sm] = -1;
				actionset[setsize++] = sm;
			}

			// process reduces
			for( i = laindex[t->number]; i < laindex[t->number+1]; i++ )
				for( sm = 0, la = LA + i * termset, end = la + termset; la < end;) {
					int bit = *la++;
					if( !bit ) sm += BITS;
					else for( int e = 0; e < BITS; e++ ) {
						if( bit & (1<<e) ) {
							if( next[sm] == -2 ) {
								// OK
								next[sm] = larule[i];
								actionset[setsize++] = sm;
							} else if( next[sm] == -1 ) {
								switch( compare_prio( larule[i], sm ) ) {
								case 0: // shift/reduce
									err->error( 1, "\ninput:" );
									print_input( t->number );
									err->error( 1, "\nconflict: shift/reduce (%i, next %s)\n", t->number, sym[sm].name );
									print_rule( 1, larule[i] );
									sr++;
									break;
								case 1: // shift
									err->error( 1, "\ninput:" );
									print_input( t->number );
									err->error( 1, "\nfixed: shift: shift/reduce (%i, next %s)\n", t->number, sym[sm].name );
									print_rule( 1, larule[i] );
									break;
								case 2: // reduce
									err->error( 1, "\ninput:" );
									print_input( t->number );
									err->error( 1, "\nfixed: reduce: shift/reduce (%i, next %s)\n", t->number, sym[sm].name );
									print_rule( 1, larule[i] );
									next[sm] = larule[i];
									break;
								}
							} else {
								// reduce/reduce
								err->error( 1, "\ninput:" );
								print_input( t->number );
								err->error( 1, "\nconflict: reduce/reduce (%i, next %s)\n", t->number, sym[sm].name );
								print_rule( 1, next[sm] );
								print_rule( 1, larule[i] );
								rr++;
							}
						}
						sm++;
					}
				}

			// insert into action_table
			int rpos = x_realloc( (void**)&action_table, nactions_used, nactions, 2*(setsize+1), sizeof(short));
			action_index[t->number] = -3-rpos;
			p = action_table + rpos;
			f = actionset;
			for( i = 0; i < setsize; i++ ) { *p++ = *f; *p++ = next[*f++]; }
			*p++ = -1;
			*p++ = -2;
		}
	}
	if( sr+rr ) err->error( 0, "conflicts: %i shift/reduce and %i reduce/reduce\n", sr, rr );
	delete[] actionset;
	delete[] next;
}


//	cleanup - destroys temporary sets
void GrammarBuilder::cleanup()
{
	delete[] derives[nterms];
	delete[] (derives+nterms);
	delete[] (firsts + nterms * varset);
	delete[] (ruleforvar + nterms * ruleset);

	for( int i = 0; i < nstates; i++ ) {
		delete[] state[i]->shifts;
		delete[] (char *)state[i];
	}
	delete[] state;

	delete[] LA;
	delete[] laindex;
	delete[] larule;
}


//	clear - destroys the content of the result variable
void GrammarBuilder::clear( Result *res )
{
	unsigned i;

	for( i = 0; i < res->nsyms; i++ ) {
		delete[] res->sym[i].type;
		delete[] res->sym[i].name;
	}
	delete[] res->sym;

	for( i = 0; i < res->rules; i++ ) {
		delete[] res->raction[i];
	}

	delete[] res->rleft;
	delete[] res->rright;
	delete[] res->rindex;
	delete[] res->rlines;
	delete[] res->raction;
	delete[] res->rprio;

	delete[] res->sym_goto;
	delete[] res->sym_to;
	delete[] res->sym_from;

	delete[] res->action_index;
	delete[] res->action_table;
}


// init - initializes internal class variables
void GrammarBuilder::init( IError *error, int debug )
{
	GrammarBuilder::err = error;
	debuglev = debug;
	errors = 0;

	sym = NULL; 
	nprio = nsyms = nterms = rules = situations = right_used = 0;
	input = eoi = errorn = -1;
	rleft = rright = rindex = rlines = rprio = priorul = NULL;
	raction = NULL;
}

// rollback the generation
void GrammarBuilder::rollback()
{
	int i;

	for( i = 0; i < nsyms; i++ ) {
		delete[] sym[i].type;
		delete[] sym[i].name;
	}
	delete[] sym;

	for( i = 0; i < rules; i++ ) {
		delete[] raction[i];
	}

	delete[] rleft;
	delete[] rright;
	delete[] rindex;
	delete[] rlines;
	delete[] raction;
	delete[] rprio;
	delete[] priorul;
}


//	generate - an entry point to the table creation mechanism
int GrammarBuilder::generate( Result *res )
{
	if( input == -1 ) {
		err->error( 0, "input symbol is not defined\n" );
		return 0;
	}

	if( eoi == -1 ) {
		err->error( 0, "the end-of-input symbol is not defined\n" );
		return 0;
	}

	if( errors ) {
		rollback();
		return 0;
	}

	// grammar
	fix_grammar();
	verify_grammar();
	nvars = nsyms - nterms;

	LA = NULL;
	laindex = larule = NULL;

	// engine
	if( !LR0() ) {
		cleanup();
		rollback();
		return 0;
	}

	lalr();
	action();
	cleanup();

	delete[] priorul;

	res->sym = sym;
	res->rules = rules;
	res->nsyms = nsyms;
	res->nterms = nterms;
	res->nstates = nstates;
	res->rleft = rleft;
	res->rright = rright;
	res->rindex = rindex;
	res->rprio = rprio;
	res->rlines = rlines;
	res->raction = raction;

	res->sym_goto = term_goto;
	res->sym_from = term_from;
	res->sym_to = term_to;
	res->action_index = action_index;
	res->action_table = action_table;
	res->nactions = nactions;
	res->errorn = errorn;

	return 1;
}
