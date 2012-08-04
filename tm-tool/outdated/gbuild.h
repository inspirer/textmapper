/*   gbuild.h
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

#ifndef gbuild_h_included
#define gbuild_h_included

#include "common.h"

/*****************************************
 * gbuild.h/cpp,engine.cpp
 * GrammarBuilder class 
 *****************************************

 **** Preamble

	First of all, init function must be called to initialize all internal
	variables. Now you can setup the grammar. Functions set_input and
	set_eoi must be called only once. Functions symbol, terminal and rule
	are designed to update grammar information database. At last you must
	call either generate function or rollback, if you skip this, you will
	have memory leaks. If call to generate function succeeded, i.e. 
	returned value is non-zero, you must call clear method to free memory,
	occupied by Result.

 **** public Methods:

 *	void init( IError *error, int debug );
	DESCR: initializes internal class variables
	PARAMS:
		error - pointer to class which realizes IError interface
		debug - 0=no_debug, 1=debug, 2=extended_debug

 *  int symbol( char *sname, int def_type, char *type, int sibling );
    DESCR: saves symbol for subsequent processing and returns symbol's number
		or registers attributes definitions
	PARAMS:
		sname - name of the symbol
		def_type -	0:  symbol from right part of rule, or define attr for sname (see sibling)
					1:  symbol from left part of rule
					2:  create new symbol for L-attrib definition
		t - symbol's type ( NULL is allowed )
		sibling - depends on def_type
			def_type == 0 
				sibling == -2		add symbol from right part of rule
				sibling == -1		declare that sname must have attribute
				sibling >=  0		declare that sname must be preceeded by the sibling
			def_type == 2
				sibling must be reference to the owner of new
				attribute. The type of attribute will be equal to
				the type of its owner.

 *  int terminal( char *name, char *type );
    DESCR: like symbol, but for tokens. All calls to terminal must precede 
	the first call to symbol

 *	void set_input( int i );
	DESCR: define the main symbol

 *	void set_eoi( int i );
	DESCR: define the end_of_input symbol

 * 	void rule( unsigned length, int priority, char *action, int *array, int defline );
	DESCR: saves the rule for processing
	PARAMS:
		length - size of right part of the rule
		priority - the rule priority ( symbol number, -1 to calculate )
		action - string (semantic action)
		array - contains the rule. array[0] - left part, array[1..length] - right part

 *	int generate( Result *res );
	DESCR: an entry point to the table creation mechanism
	PARAMS:
		res - result of operation
	RETVAL:
		nonzero on success
	COMMENTS:
		don't forget to clear the result

 *	void rollback();
	DESCR: rollback the generation


 *	static void clear( Result *res );
	DESCR: destroys the content of the result variable

 **** internal Variables:


*****************************************/

#define CH_SIZE 1023

class GrammarBuilder {
public:
	struct Symbol {
		char *name, *type;
		int  opt, sibling;
		int  length, rpos;
		unsigned empty:1, term:1, good:1, employed:1, defed:1, 
				 temp:1, is_attr:1, has_attr:1;
	};

	struct State {
		int  fromstate, symbol, number, nshifts, nreduce;
		State *link, *next;
		short *shifts, *reduce;
		unsigned LR0:1;
		short elems[1];
	};

	struct Result {
		Symbol *sym;
		unsigned rules, nsyms, nterms, nstates, errorn;
		int *rleft, *rright, *rindex, *rprio, *rlines;
		char **raction;
		short *sym_goto, *sym_from, *sym_to, *action_table;
		int *action_index;
		unsigned nactions;
	};

	struct Short {
		short value;
		Short *next;
	};

private:

	// init
	int debuglev;
	IError *err;
	int errors;

	// grammar information
	Symbol *sym;
	unsigned nsyms, nterms;
	int input, eoi, errorn;

	int *rleft, *rindex, *rright, *rprio, *rlines;
	int *priorul, nprio;
	char **raction;
	unsigned rules, situations, right_used;

	// LR0 engine
	unsigned nvars;
	unsigned termset, varset, ruleset, sitset;
	int nstates, *firsts, *ruleforvar;
	short **derives;
	State **state;

	short *toreduce, *closure, *closureend;
	short **symbase, **symend, *symcanshift;
	int   *closurebit, ntoreduce, ntoshift;
	State **table, *current, *last, *first;

	// last generation
	int   *action_index;
   	unsigned nactions, nactions_used;
	short *action_table;
	short *term_goto, *term_from, *term_to;

	// gbuild.cpp
	void fix_grammar();
	void verify_grammar();
	void print_input( int s );
	int  compare_prio( int rule, int next );  //  0:unresolved 1:shift 2:reduce
	void action();
	void cleanup();
	
	// engine.cpp : LR0
	int  LR0();
	void allocate_data();
	void initializeLR0();
	void build_derives();
	void build_sets();
	State *new_state( int from, int by, unsigned hash, int size );
	short goto_state( int symbol );
	int  process_state();
	void build_closure( short *prev );
	void print_situation( int errl, int situation );
	void print_rule( int errl, int rule );
	void insert_shift( State *t, int tostate );
	void add_final_states();
	void show_debug();
	void freeLR0();

	// engine.cpp : graph closure
	int infinity, top;
	short **relation, *gc_index, *gc_vertices;
	void do_vertex( int i );
	void graph_closure( short **relation );

	// lalr engine
	int maxrpart, ngotos, ntgotos;
	short *larule, *laindex;
	Short **lookback;
	short *nterm_goto, *nterm_from, *nterm_to;
	short *edge, **graph;
	int *LA, *follow;

	// engine.cpp : lalr
	void lalr();
	void initializeLA();
	void init_goto();
	int  select_goto( int state, int symbol );
	void add_lookback( int state, int rule, short gotono );
	void transpose_graph();
	void init_follow();
	void build_follow();
	void buildLA();
	void show_follow();
	void show_graph();
	void show_lookaheads();
	void freeLA();

public:
	void init( IError *error, int debug );
	int  symbol( char *name, int def_type, char *type = NULL, int sibling = -2 );
	int  terminal( char *name, char *type = NULL );
	void set_input( int number );
	void set_eoi( int number );
	void rule( unsigned length, int priority, char *action, int *array, int defline );
	void addprio( char *id, int prio, int restofgroup );
	int  generate( Result *res );
	void rollback();
	static void clear( Result *res );
};

#endif
