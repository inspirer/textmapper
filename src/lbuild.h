/*   lbuild.h
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

#ifndef lbuild_h_included
#define lbuild_h_included

#include "common.h"

/*****************************************
 * lbuild.h/cpp
 * LexicalBuilder class 
 *****************************************

 **** Preamble

	First of all, init function must be called to initialize all internal
	variables. Now you can setup the lexical analyzer using lexem function. 
	At last you must call either generate function or rollback, if you skip 
	this, you will have memory leaks. If the call to generate function succeeded,
	i.e. returned value is non-zero, you must later call clear method to free 
	memory, occupied by Result.

 **** public Methods:

 *	void init( IError *error, int debug );
	DESCR: initializes internal class variables
	PARAMS:
		error - pointer to class which realizes IError interface
		debug - 0=no_debug, 1=debug, 2=extended_debug

 *  int  lexem( int num, char *regexp, char *name, char *action, int priority=0 );
	DESCR:   adds lexem to database, for subsequent tables generation
	PARAMS:  num - identifying number of the lexem (ex: returned from GrammarBuilder::symbol)
			 regexp - regular expression
			 name - symbolic name of the lexem, used for errors reporting
			 action - semantic actions which will be performed after lexem is encountered
			 priority - lexem's priority

 *	int generate( Result *res );
	DESCR: an entry point to the table creation mechanism
	PARAMS:
		res - result of operation
	RETVAL:
		nonzero on success
	COMMENTS:
		don't forget to clear the result if non-zero is returned

 *	void rollback();
	DESCR: rollback the generation

 *	static void clear( Result *res );
	DESCR: destroys the content of the result variable


 **** internal Variables:

  ** current database variables

	Lexems are enumerated from 0 to nterms-1. 'nterms' is incremented by
		LexicalBuilder::lexem on each successfull completion.

	symbols[SIZE_SYM] - contains a set of used symbols. If some symbol
		was used in one of regular expressions, then corresponding bit in 
		this array is set. This array is filled in LexicalBuilder::getnext
		method.

	sym[MAX_ENTRIES], stack[MAX_DEEP] - this variables are temporary and
		used by LexicalBuilder's 'lexem' and 'BuildJumps' methods.

	lexemerrors - low 28 bits is total number of errors encountered by 
		Lexical Builder, 28 bit is set if the maximum number of lexems is reached.

	setpool - when LexicalBuilder::getnext encounters a new set in [], it 
		stores the last one in this pool, and returns the index of the 
		first set element

	pool_used, pool_size - used by x_realloc, to reallocate space in the setpool

	lname[nterms], 
	lnum[nterms],
	lact[nterms],
	lprio[nterms] - name, number, action and priority of the given lexem
		All the arrays are filled in LexicalBuilder::lexem method.

	nsit - is the length of a lsym array

	lsym[nsit] is the common storage for all regexps, so each lexem in 
		lsym begins at pos lindex[lexem], and it's length is llen[lexem].
		At the position 'lindex[lexem]+llen[lexem]-1' you can find
		the value '-1-lexem'. Each element of this array can be the 
		following:

			LBR+i		left bracket. i represents the number of 
						the corresponding right bracket (stored in low 16bits), 
						i.e. lsym[lindex[]+i] is right bracket
			OR			'|'	(Alternation)
			RBR			right bracket
			ANY			'.'
			SPL			splitter, inserted after each * or +, to forbid
						two steps back in reqular expressions like /a*b+/
			SYM+n		One symbol, value of the symbol is stored in low 8bits.

			>0			Set. Number is the index in the setpool array.

            -1..-0x100000   '-1-Number' of the reached lexem

        30th and 29th bits contains qualifier: 0 = none, 1 = +, 2 = *, 3 = ?

	ljmpset[i] = SIZE(llen[i]) is the size of the set.

	ljmp[nterms] - for each lexem ljmp[lexem] is an array of llen[lexem] sets.
			Each set has llen[lexem] elements.

    currentgroups - specifies the current set of groups the next lexem is belong to

  ** run-time generated variables

*****************************************/

enum {
	MAX_LEXEMS = 0x100000,
	MAX_ENTRIES = 1024,
	MAX_DEEP = 128,
	SIZE_SYM = SIZE(256),
	HASH_SIZE = 1023
};

class LexicalBuilder {
public:
	struct State {
		State *next, *hash;
		int number;
		word *change;
		int set[1];
	};

	struct Result {
		unsigned int groupset[BITS];
		int nstates, nchars, nterms;
		char **lact;
		int *lnum, *char2no;
		State **dta;
	};

	unsigned int currentgroups, totalgroups;

private:
	int debuglev;
	IError *err;

	// current database variables
	int symbols[SIZE_SYM];
	int sym[MAX_ENTRIES], stack[MAX_DEEP];
	unsigned lexemerrors, pool_used, pool_size;
	int *setpool;

	unsigned int *group, groupset[BITS];
	unsigned nsit, nterms, sym_used;
	int *lnum, *lprio, *lsym, *lindex, *llen, *ljmpset, **ljmp;
	char **lact, **lname;

	// generate-time variables
	int characters, *char2no, *no2char;

	int states;
	State *hash[HASH_SIZE];
	State *first, *last, *current;
	int *clsr, *cset;
	
	// methods
	int  getnext( char *&t );
	int  add_set( int *state, int size, int sum );
	void closure( int *set, int &length );
	void BuildJumps();
	void BuildArrays();
	int  BuildStates();
	void cleanup();

public:

	// public interface
	void init( IError *error, int debug );
	int  lexem( int num, char *regexp, char *name, char *action, int priority=0 );
	int  generate( Result *res );
	void rollback();
	static void clear( Result *res );
};

#endif
