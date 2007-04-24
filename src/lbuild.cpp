/*   lbuild.cpp
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
#include "lbuild.h"

//#define LEX_CLOSURE_DEBUG

enum {
	LBR  = 0x80010000,
	RBR  = 0x80020000,
	 OR  = 0x80030000,
	SPL  = 0x80040000,
	ANY  = 0x80050000,
	SYM  = 0x80060000,
	MASK = 0x800f0000,

	HIGH_STORAGE = (1<<29)+(1<<30),
	ALREADYMAXLEXEM = 0x10000000
};


// init - initializes internal class variables
void LexicalBuilder::init( IError *error, int debug )
{
	debuglev = debug;
	LexicalBuilder::err = error;

	ZERO( symbols, 256 );
	SET( symbols, 0 );
	SET( symbols, 1 );

	currentgroups = 1;
	totalgroups = lexemerrors = 0;
	pool_used = pool_size = 0;
	setpool = NULL;

	nsit = nterms = sym_used = 0;
	lnum = lprio = lsym = lindex = llen = ljmpset = NULL;
	group = NULL;
	lname = lact = NULL;
	ljmp = NULL;
}


// escape - returnes the value of escaped character
static inline int escape( int c )
{
	switch( c ) {
		case 'a': return 7;
		case 'b': return '\b';
		case 'f': return '\f';
		case 'n': return '\n';
		case 'r': return '\r';
		case 't': return '\t';
		case 'v': return '\v';
		default: return c;
	}
}


// getnext - returns next lexem from regexp, updates t and s pointers
int LexicalBuilder::getnext( char *&t )
{
	int res, i, e, invert = 0;
	int set[SIZE_SYM];

	switch( *t ) {
		case '(': 
			return t++, LBR;

		case ')': 
			return t++, RBR;

		case '|': 
			return t++, OR;

		case '.':
			SET( symbols, '\n' );
			return t++, ANY;

		case '[':
			if( *++t == '^' ) invert = 1, t++;

			ZEROSET( set, SIZE_SYM );
			while( *t && *t != ']' ) {
				if( (i = *t) == '\\' ) {
					if( !*++t )
						goto backslash_error;
					i = escape( *t );
				}
				SET( set, i );
				if( *++t == '-' ) {
					e = *++t;
					if( e && e != ']' ) {
						if( e == '\\' ) {
							if( !*++t ) 
								goto backslash_error;
							e = escape( *t );
						}

						if( e > i )
							for( i++; i <= e; i++ )
								SET( set, i );
						else 
							for( i--; i >= e; i-- )
								SET( set, i );

						t++;
					} else t--;
				}
			}

			if( !*t ) {
				err->error( 0, "lex: enclosing square bracket not found:" );
				return -1;
			}

			for( i = 0; i < SIZE_SYM; i++ )
				symbols[i] |= set[i];

			if( invert ) {
				for( i = 0; i < SIZE_SYM; i++ )
					set[i] = ~set[i];
				CLEAR( set, 0 );
			}

			res = e = x_realloc( (void**)&setpool, pool_used, pool_size, SIZE_SYM, sizeof(int) );
			for( i = 0; i < SIZE_SYM; i++, e++ )
				setpool[e] = set[i];

			return t++, res;

		case '\\':
			t++;
			if( !*t ) {
				backslash_error:
				err->error( 0, "lex: \\ found at the end of expression:" );
				return -1;
			}
			i = escape( *t );
			SET( symbols, i );
			return t++, i|SYM;

		default:
			i = *t;
			SET( symbols, i );
			return t++, i|SYM;
	}
}


//	lexem - adds new lexem to the pool
int LexicalBuilder::lexem( int num, char *regexp, char *name, 
							   char *action, int priority )
{
	char *l = regexp;
	int  length = 0, e = 1;
	int  addbrackets = 0;

	if( nterms == MAX_LEXEMS-1 ) {
		if( (lexemerrors & ALREADYMAXLEXEM) == 0 ) 
			err->error( 0, "lex: too much lexems\n" );
		lexemerrors |= ALREADYMAXLEXEM;
		return 0;
	}

	if( !currentgroups ) {
		err->error( 0, "lex: new lexem %s does not belong to groups\n", name );
		lexemerrors++;
		return 0;
	}

	totalgroups |= currentgroups;

	if( *l == 0 ) {
		err->error( 0, "lex: regexp for `%s' does not contain symbols\n", name );
		lexemerrors++;
		return 0;
	}

	if( !strcmp( name, "error" ) ) {
		err->error( 0, "lex: error token must be defined without regular expression\n", name );
		lexemerrors++;
		return 0;
	}

	while( *l ) {
		if( length > MAX_ENTRIES-5 ) {
			err->error( 0, "lex: regular expression is too long: /%s/\n", regexp );
			lexemerrors++;
			return 0;
		}

		sym[length] = getnext( l );

		switch( sym[length] ) {
			case LBR:
				stack[e] = length;
				if( ++e >= MAX_DEEP ) {
					err->error( 0, "lex: regular expression is too deep: /%s/\n", regexp );
					lexemerrors++;
					return 0;
				}
				break;
			case OR:
				if( e == 1 ) addbrackets = 1; break;
			case RBR:
				if( --e == 0 ) goto error;
				sym[stack[e]] |= length;
				/* FALLTHROUGH */
			default:
				if( *l == '+' || *l == '?' || *l == '*' ) {
					switch( *l ) {
						case '+': sym[length] |= 1<<29; break;
						case '*': sym[length] |= 2<<29; break;
						case '?': sym[length] |= 3<<29; break;
					}
					if( *l != '?' ) 
						sym[++length] = SPL;
					l++;
				}
				break;
			case -1:
				err->error( 3, " /%s/\n", regexp );
				lexemerrors++;
				return 0;
		}
		length++;
	}

	error:
	if( e != 1 ) { 
		err->error( 0, "lex: error in using parantheses: /%s/\n", regexp );
		lexemerrors++;
		return 0;
	}

	if( addbrackets ) {
		for( e = 0; e < length; e++ )
			if( (sym[e]&MASK) == LBR )
				(*(short*)&sym[e])++;

		sym[length++] = RBR;
	}

	sym[length++] = -1-nterms;

	// reallocate memory
	inc_realloc( (void**)&lnum, nterms, sizeof(int) );
	inc_realloc( (void**)&lact, nterms, sizeof(char*) );
	inc_realloc( (void**)&lprio, nterms, sizeof(int) );
	inc_realloc( (void**)&group, nterms, sizeof(unsigned int) );
	inc_realloc( (void**)&lindex, nterms, sizeof(int) );
	inc_realloc( (void**)&llen, nterms, sizeof(int) );
	inc_realloc( (void**)&ljmp, nterms, sizeof(int*) );
	inc_realloc( (void**)&ljmpset, nterms, sizeof(int) );
	inc_realloc( (void**)&lname, nterms, sizeof(char*) );

	int tlength = length + addbrackets;

	e = x_realloc( (void**)&lsym, sym_used, nsit, tlength, sizeof(int) );

	// save all lexem info
	lnum[nterms] = num;
	lact[nterms] = _strdup(action);
	lname[nterms] = _strdup(name);
	lprio[nterms] = priority;
	group[nterms] = currentgroups;
	lindex[nterms] = e;
	llen[nterms] = tlength;
	ljmpset[nterms] = SIZE(tlength);
	ljmp[nterms] = new int[tlength*ljmpset[nterms]];

	memset( ljmp[nterms], 0, tlength*ljmpset[nterms]*sizeof(int) );

	if( addbrackets )
		lsym[e++] = LBR|(length-1);

	for( int i = 0; i < length; i++, e++ )
		lsym[e] = sym[i];

	nterms++;
	return 1;
}


//	fills: ljmp
void LexicalBuilder::BuildJumps()
{
	int i, k, lex;

	if( debuglev >= 2 )
		err->error( 2, "\nLexem jumps:\n" );

	for( lex = 0; lex < nterms; lex++ ) {

		int *jumps = ljmp[lex], jmpset = ljmpset[lex];
		int cd = -1, len = llen[lex] - 1;
		int *sym = lsym + lindex[lex];

		// generate initial jumps
		for( i = 0; i < len; i++ ) switch( sym[i]&MASK ) {
			case LBR: // (
				stack[++cd] = i;
				SET( jumps + i*jmpset, i+1 );
				break;

			case RBR: // )
				SET( jumps + i*jmpset, i+1 );

				switch( (sym[i]>>29)&3 ) {
					case 1: // +
						SET( jumps + (i+1)*jmpset, stack[cd] );
						break;
					case 2: // *
						SET( jumps + (i+1)*jmpset, stack[cd] ); 
						SET( jumps + stack[cd]*jmpset, i+1 );
						break;
					case 3: // ?
						SET( jumps + stack[cd]*jmpset, i+1 );
						break;
				}
				cd--;
				break;

			case OR:  // |
				k = sym[stack[cd]]&0xffff;
				SET( jumps + i*jmpset, k+1 );
				SET( jumps + stack[cd]*jmpset, i+1 );
				break;

			case SPL:  // forbid two steps back
				SET( jumps + i*jmpset, i+1 );
				break;

			default: // SYM, ANY, SET
				switch( (sym[i]>>29)&3 ) {
					case 1: // +
						SET( jumps + (i+1)*jmpset, i );
						break;
					case 2: // *
						SET( jumps + (i+1)*jmpset, i ); 
						SET( jumps + i*jmpset, i+1 );
						break;
					case 3: // ?
						SET( jumps + i*jmpset, i+1 );
						break;
				}
				break;
		}

		// transitive closure of jumps
		int j, e;
		for( i = 0; i <= len; i++ )
			for( j = 0; j <= len; j++ )
				if( IS( jumps + jmpset*j, i ) )
					for( e = 0; e <= len; e++ )
						if( IS( jumps + jmpset*i, e ) )
							SET( jumps + jmpset*j, e );

		// reflexive
		for( i = 0; i <= len; i++ )
			SET( jumps + jmpset*i, i );

		// extended debug information
		if( debuglev >= 2 ) {
			err->error( 2, "%2i: ", lex );
			for( i = 0; i < len; i++ ) {
				err->error( 2, " (%i:", i );
				for( k = 0; k <= len; k++ )
					if IS(jumps + i*jmpset, k) 
						err->error( 2, " %i", k );
				err->error( 2, ") " );
				switch( sym[i]&MASK ) {
					case LBR: err->error( 2, "LEFT(%i)", sym[i]&0xffff );break;
					case RBR: err->error( 2, "RIGHT" );break;
					case OR:  err->error( 2, "OR" );break;
					case SPL: err->error( 2, "SPLIT" );break;
					case ANY: err->error( 2, "ANY" );break; 
					case SYM:
						if( (sym[i]&0xff) < 127 && (sym[i]&0xff) > 32 )
							err->error( 2, "\'%c\'", sym[i]&0xff ); 
						else
							err->error( 2, "#%02x", sym[i]&0xff );
						break;
					default:  err->error( 2, "SET#%i", (sym[i]&~HIGH_STORAGE)/SIZE_SYM ); break;
				}
				switch( (sym[i]>>29)&3 ) {
					case 1: err->error( 2, "+" );break;
					case 2: err->error( 2, "*" );break;
					case 3: err->error( 2, "?" );break;
				}
			}
			err->error( 2, "  [%s,%i]\n", lname[lex], lnum[lex] );
		}
	}

	if( debuglev >= 2 )
		err->error( 2, "\n" );
}


//	fills: char2no, no2char, characters
void LexicalBuilder::BuildArrays()
{
	int i, e;
	
	char2no = new int[256];
	no2char = new int[256];
	
	for( i = 0; i < 256; i++ ) 
		char2no[i] = 1;

	for( characters = e = 0; e < SIZE_SYM; e++ )
		if( symbols[e] )
			for( i = 0; i < BITS; i++ )
				if( symbols[e] & (1<<i) ) {
					no2char[characters] = e*BITS+i;
					char2no[e*BITS+i] = characters++;
				}
}


//	closure - builds closure of the given set (using jumps)
void LexicalBuilder::closure( int *set, int &length )
{
	int *n, *p = set;
	int i = 0, clind = 0, base, slen;
	int k, l, m, word;

	#ifdef LEX_CLOSURE_DEBUG
		err->error( 2, "\tclosure of: " );
		for( n = set; *n >= 0; n++ ) err->error( 2, " %i", *n );
		err->error( 2, "\n" );
	#endif

	while( *p >= 0 ) {

		// search for next terminal
		while( i < nterms && *p >= lindex[i+1] ) i++;
		ASSERT( i < nterms );

		// create closure for it in cset
		slen = ljmpset[i];
		ZEROSET( cset, slen );
		for( base = lindex[i], word = lindex[i+1]; *p >= 0 && *p < word; p++ ) {
			ASSERT( *p >= base );
			n = ljmp[i] + (*p - base) * slen;
			for( l = 0; l < slen; l++ )
				cset[l] |= *n++;
		}

		#ifdef LEX_CLOSURE_DEBUG
		    err->error( 2, "\t\t\tcset (%i, base=%i) = ", i, base ); 
			for( l = 0; l < slen*BITS; l++ )
				if IS(cset, l) 
					err->error( 2, " %i(%02x)", l, lsym[base+l] );
			err->error( 2, "\n" );
		#endif
		
		// save cset in closure (exclude LBR, RBR, SPL, OR)
		for( m = base, k = 0; k < slen; k++ )
			if( (word = cset[k]) == 0 )
				m += BITS;
			else for( l = 0; l < BITS; l++, m++ )
				if( word & (1<<l) )
					if( (int)(lsym[m] & MASK) > (int)SPL ) 
						clsr[clind++] = m;
	}

	// save closure in initial array
	for( p = set, n = clsr, i = 0; i < clind; i++ )
		*p++ = *n++;
	*p++ = -1;
	length = clind;

	#ifdef LEX_CLOSURE_DEBUG
		err->error( 2, "\t\t\tis: " );
		for( n = set; *n >= 0; n++ ) err->error( 2, " %i", *n );
		err->error( 2, "\n" );
	#endif
}


// add: -1 terminated 'state' of the given size
int LexicalBuilder::add_set( int *state, int size, int sum )
{
	State *n;
	int *p, *q;

	ASSERT( sum >= 0 );

	// search for existing
	for( n = hash[sum]; n; n = n->hash ) {
		for( p = n->set, q = state; *p >= 0 && *q >= 0 && *p == *q; p++, q++);
		if( *p == -1 && *q == -1 )
			return n->number;
	}

	// have we exceed the limits
	if( states >= MAX_WORD )
		return -1;

	// create new
	n = (State *)new char[sizeof(State) + size*sizeof(int)];
	last = last->next = n;
	n->hash = hash[sum];
	hash[sum] = n;
	n->next = NULL;

	n->number = states++;
	n->change = NULL;
	for( p = n->set, q = state; *q >= 0; )
		*p++ = *q++;
	*p++ = -1;

	ASSERT( p - n->set == size + 1 );
	return n->number;
}


//	BuildStates - generates the result
int LexicalBuilder::BuildStates()
{
	int i, e, k;
	int *cs, *next, nnext;
	int toshift[SIZE_SYM];

	// allocate temporary storage
	clsr = new int[nsit];
	next = new int[nsit+1];
	nnext = 0;

	// fix lindex and allocate temporary set
	inc_realloc( (void**)&lindex, nterms, sizeof(int) );
	lindex[nterms] = nsit;
	for( e = 1, i = 0; i < nterms; i++ )
		if( ljmpset[i] > e ) e = ljmpset[i];
	cset = new int[e];

	// create first set
	for( i = 0; i < nterms; i++ ) 
		if( group[i] & 1 )
			next[nnext++] = lindex[i];
	next[nnext] = -1;
	closure( next, nnext );
	for( e = 0, cs = next; *cs >= 0; cs++ ) e += *cs;
	e = (unsigned)e % HASH_SIZE;
	groupset[0] = 0;

	// create state
	states = 1;
	memset( hash, 0, HASH_SIZE*sizeof(State*) );
	current = first = last = (State *) new char[ sizeof(State) + nnext*sizeof(int) ];
	first->number = 0;
	first->hash = first->next = NULL;
	hash[e] = first;
	for( i = 0; i <= nnext; i++ )
		first->set[i] = next[i];

	// create left group states
	for( k = 1; k < BITS; k++ )
		if( totalgroups & (1<<k) ) {
			for( nnext = i = 0; i < nterms; i++ ) 
				if( group[i] & (1<<k) )
					next[nnext++] = lindex[i];
			next[nnext] = -1;
			closure( next, nnext );
			for( e = 0, cs = next; *cs >= 0; cs++ ) e += *cs;
			groupset[k] = add_set( next, nnext, (unsigned)e % HASH_SIZE );
		} else groupset[k] = (unsigned)-1;

	// generate states
	while( current ) {

		// first of all we must search if there any lexem have been read already
		int lexnum = -1;
		ZERO( toshift, 256 );
		for( cs = current->set; *cs >= 0; cs++ )
			if( lsym[*cs] < 0 && lsym[*cs] >= -MAX_LEXEMS ) {

				// end of some regexp found
				const int nlex = -1-lsym[*cs];
				if( lexnum != -1 && lexnum != nlex ) {

					if( lprio[nlex] == lprio[lexnum] ) {
						err->error( 0, "lex: two lexems are identical: %s and %s\n", lname[lexnum], lname[nlex] );
						lexemerrors++;

					} else if( lprio[nlex] > lprio[lexnum] ) {
						if( debuglev ) 
							err->error( 1, "fixed: %s > %s\n", lname[nlex], lname[lexnum] );
						lexnum = nlex;

					} else if( debuglev ) 
						err->error( 1, "fixed: %s > %s\n", lname[lexnum], lname[nlex] );

				} else lexnum = nlex;
		
			} else {
				switch( lsym[*cs]&MASK ) {
					case SYM: 
						SET( toshift, lsym[*cs]&0xff );
						break;
					case ANY: 
						for( i = 1; i < SIZE_SYM; i++ )
							toshift[i] = ~0;
						toshift[0] |= ~((1<<'\n')+(1));
						break;
					default: 
						e = lsym[*cs]&~HIGH_STORAGE;
						for( i = 0; i < SIZE_SYM; i++, e++ )
							toshift[i] |= setpool[e];
						break;
				}
			}

		// check for the empty lexem
		if( current == first && lexnum != -1 ) {
			err->error( 0, "lex: lexem is empty: `%s`\n", lname[lexnum] );
			lexemerrors++;
		}


		// allocate new change table
		current->change = new short[characters];

		// try to shift all available symbols
		for( i = 0; i < characters; i++ )
			if IS( toshift, no2char[i] ) {
				int *p, *t, *o, l, sym = no2char[i];

				// create new state
				for( t = next, p = current->set; *p >= 0; p++ ) {
					l = lsym[*p];

					if( (l&MASK) == ANY ) {
						if( sym != '\n' ) 
							*t++ = *p + 1;
					} else if( (l&MASK) == SYM ) {
						if( sym == (l&0xff) ) 
							*t++ = *p + 1;
					} else if( l >= 0 ) {
						o = &setpool[l&~HIGH_STORAGE];
						if IS(o,sym) 
							*t++ = *p + 1;
					}
				}
				nnext = t - next;

				// closure
				*t = -1;
				closure( next, nnext );

				// save new state
				for( l = e = 0; e < nnext; e++ )
					l += next[e];

				current->change[i] = add_set( next, nnext, (unsigned)l % HASH_SIZE );

				// Have we exceeded the limits?
				if( current->change[i] == -1 ) {
					err->error( 0, "lex: lexical analyzer is too big ...\n" );
					lexemerrors++;
					goto out;
				}

			} else current->change[i] = (lexnum>=0) ? -2-lnum[lexnum] : -1;

		ASSERT( current->change[0] < 0 );

		// next state
		current = current->next;
	}

	first->change[0] = -2;

out:
	delete[] cset;
	delete[] next;
	delete[] clsr;

	return (lexemerrors == 0);
}


//	cleanup - deletes the temporary variables
void LexicalBuilder::cleanup()
{
	int i;
	
	delete[] lprio;
	delete[] group;
	delete[] lsym;
	delete[] lindex;
	delete[] setpool;
	delete[] ljmpset;
	delete[] llen;
	delete[] no2char;

	for( i = 0; i < nterms; i++ )
		delete[] lname[i];
	delete[] lname;
	
	for( i = 0; i < nterms; i++ )
		delete[] ljmp[i];
	delete[] ljmp;
}


//	clear - destructor for the result structure
void LexicalBuilder::clear(Result *res)
{
	int i;
	
	delete[] res->lnum;
	delete[] res->char2no;

	for( i = 0; i < res->nterms; i++ )
		delete[] res->lact[i];
	delete[] res->lact;

	for( i = 0; i < res->nstates; i++ ) {
		delete[] res->dta[i]->change;
		delete[] (char*)res->dta[i];
	}
	delete[] res->dta;
}


//	generate - create tables
int LexicalBuilder::generate(Result *res)
{
	if( nterms == 0 ) {
		err->error( 0, "lex: no lexems\n" );
		res->nterms = res->nstates = res->nchars = 0;
		return 0;
	}

	if( ! (totalgroups & 1 ) ) {
		err->error( 0, "lex: no lexems in the first group\n" );
		rollback();
		return 0;
	}

	if( lexemerrors ) {
		rollback();
		return 0;
	}

	BuildJumps();
	BuildArrays();
	int r = BuildStates();
	cleanup();

	res->dta = new State*[states];
	for( State *s = first; s; s = s->next )
		res->dta[s->number] = s;

	res->nterms = nterms;
	res->nstates = states;
	res->nchars = characters;
	
	res->char2no = char2no;
	res->lact = lact;
	res->lnum = lnum;

	memcpy( res->groupset, groupset, BITS*sizeof(unsigned int) );

	if( !r )
		clear(res);

	return r;
}

// remove grammar database from memory
void LexicalBuilder::rollback()
{
	if( nterms ) {
		int i;

		delete[] setpool;
		delete[] lnum;
		delete[] lprio;
		delete[] group;
		delete[] lsym;
		delete[] lindex;
		delete[] llen;
		delete[] ljmpset;

		for( i = 0; i < nterms; i++ )
			delete[] lname[i];
		delete[] lname;
	
		for( i = 0; i < nterms; i++ )
			delete[] ljmp[i];
		delete[] ljmp;

		for( i = 0; i < nterms; i++ )
			delete[] lact[i];
		delete[] lact;
	}
}
