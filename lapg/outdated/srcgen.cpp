/*	 srcgen.cpp
 *
 *	 Lapg (Lexical Analyzer and Parser Generator)
 *	 Copyright (C) 2002-07	Evgeny Gryaznov (inspirer@inbox.ru)
 *
 *	 This program is free software; you can redistribute it and/or modify
 *	 it under the terms of the GNU General Public License as published by
 *	 the Free Software Foundation; either version 2 of the License, or
 *	 (at your option) any later version.
 *
 *	 This program is distributed in the hope that it will be useful,
 *	 but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	 GNU General Public License for more details.
 *
 *	 You should have received a copy of the GNU General Public License
 *	 along with this program; if not, write to the Free Software
 *	 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

#include "common.h"
#include "srcgen.h"

extern const char *templ_cpp, *default_cpp;
extern const char *templ_c, *default_c;
extern const char *templ_cs, *default_cs;
extern const char *templ_js, *default_js;
extern const char *templ_java, *default_java;
extern const char *templ_text;

#ifdef RUN_IN_DEBUG
#define CPPDEF "parse1.cpp"
#else
#define CPPDEF "parse.cpp"
#endif

const language langs[] = {
	{ "c++", templ_cpp, NULL, default_cpp, CPPDEF, "{", "}", 1},
	{ "c", templ_c, NULL, default_c, "parse.c", "{", "}", 1},
	{ "cs",  templ_cs,  NULL, default_cs,  "parse.cs", "{", "}", 1},
	{ "js",  templ_js,  NULL, default_js,  "parse.js", "[", "]", 0},
	{ "java", templ_java, NULL, default_java, "Parser.java", "{", "}", 0},
	{ "text", templ_text, NULL, NULL, "tables.txt", "{", "}", 1},
	{ NULL }
};

// put the formatted string to the stream #0, #1 or #2
void SourceGenerator::error( int e, char *r, ... )
{
	va_list arglist;
	va_start( arglist, r );
	switch( e ) {
		case 0: case 3:
			if( e == 0 ) fprintf( stderr, "lapg: " );
			vfprintf( stderr, r, arglist );
			break;
		case 1:
			if( !err ) err = fopen( "errors", "w" );
			if( err ) vfprintf( err, r, arglist );
			break;
		case 2:
			if( !dbg ) dbg = fopen( "states", "w" );
			if( dbg ) vfprintf( dbg, r, arglist );
			break;
	}
}


// processes language independent directive
void SourceGenerator::process_directive( char *id, char *value, int line, int column )
{
	if( !strcmp( id, "class" ) ) {
		classn = value;

	} else if( !strcmp( id, "getsym" ) ) {
		getsym = value;

	} else if( !strcmp( id, "errorprefix" ) ) {
		errprefix = value;

	} else if( !strcmp( id, "namespace" ) ) {
		ns = value;

	} else if( !strcmp( id, "lang" ) ) {

		int i;

		for( i = 0; langs[i].lang; i++ )
			if( !strcmp( langs[i].lang, value ) )
				break;

		if( langs[i].lang ) {
			language = i;
		} else {
			error( 0, "lapg: %s, %i(%i) unknown language %s\n", sourcename, line, column, value );
		}

	} else if( !strcmp( id, "positioning" ) ) {
		if( !strcmp( value, "none" ) ) positioning = 0;
		else if( !strcmp( value, "line" ) ) positioning = 1;
		else if( !strcmp( value, "full" ) ) positioning = 2;
		else if( !strcmp( value, "offset" ) ) positioning = 3;
		else error( 0, "lapg: %s, %i(%i) unknown positioning value %s\n", sourcename, line, column, value );
		delete[] value;

	} else if( !strcmp( id, "lexemend" ) ) {
		if( !strcmp( value, "on" ) ) lexemend = 1;
		else if( !strcmp( value, "off" ) ) lexemend = 0;
		else error( 0, "lapg: %s, %i(%i) unknown lexemend value %s (can be on/off)\n", sourcename, line, column, value );
		delete[] value;

	} else if( !strcmp( id, "breaks" ) ) {
		if( !strcmp( value, "on" ) ) genbreaks = 1;
		else if( !strcmp( value, "off" ) ) genbreaks = 0;
		else error( 0, "lapg: %s, %i(%i) unknown breaks value %s (can be on/off)\n", sourcename, line, column, value );
		delete[] value;

	} else
		lalr1::process_directive( id, value, line, column );
}


// fills: buffer
void SourceGenerator::fillb()
{
	int size = fread( b, 1, 1024, stdin );
	b[size] = 0; end = b + size; l = b;
}


// reads the input and generate output
void SourceGenerator::process( int debug )
{
	debuglev = debug;
	err = dbg = NULL;

	// init
	ns = errprefix = classn = getsym = NULL;
	lexemend = positioning = 0;
	genbreaks = 1;
	fillb();

	if( run() ) {

		if( language == -1 )
			language = 0;

		if( !targetname )
			targetname = langs[language].output;

		if( *targetname && targetname[0]!='-' && targetname[1]!=0 )
			if( !freopen( targetname, "w", stdout ) ) {
				perror( targetname );
				goto skip_printout;
			}

		printout();
	skip_printout:
		clear();
	}

	if( err ) fclose( err );
	if( dbg ) fclose( dbg );

	// clean
	if( classn ) delete[] classn;
	if( getsym ) delete[] getsym;
	if( errprefix ) delete[] errprefix;
	if( ns ) delete[] ns;
}

static const char tabs[] = "\t\t\t\t\t\t\t\t\t\t";

#define TABS(n) ((n>=10) ? ::tabs : ::tabs + 10 - n )

// prints rule's action to stdout
void SourceGenerator::print_action( char *action, int rule, int expand_cpp, int tabcount )
{
	char *m, *p, *l = action;
	char c;
	int  *rl, length, i, e, k, num;
	int  rpos;

	if( !langs[language].addLineInfo ) {	/* skip #line */
		while( *l && strncmp(l,"#line",5) == 0 ) {
			while( *l && *l != '\n' ) l++;
			if( *l == '\n' ) l++;
		}
	}

	if( tabcount > 10 ) tabcount = 10;
	for( length = 0, rl = gr.rright+gr.rindex[rule]; *rl >= 0; length++, rl++ );
	rpos = gr.rindex[rule];
	
	if( gr.sym[gr.rleft[rule]].is_attr ) {
		ASSERT( length == 0 );
		length = gr.sym[gr.rleft[rule]].length;
		rpos = gr.sym[gr.rleft[rule]].rpos;
	}

	while( *l ) {
		if( *l == '\n' ){

			if( !langs[language].addLineInfo && strncmp(l+1,"#line",5) == 0 ) {
				l++;
				while( *l && *l != '\n' ) l++;
			} else {
				printf( "\n%s", tabs + 10 - tabcount );
				l++;
			}

		} else if( *l == '$' ) {
			l++;

			if( *l == '$' )
				printf( "lapg_gg.sym" ), l++;

			else if( *l == '#' ) {
				l++;
				if( gr.sym[gr.rleft[rule]].has_attr ) {
					i = gr.sym[gr.rleft[rule]].sibling;
					ASSERT( i>=-2 );
					if( i >= 0 && gr.sym[i].type ) 
						printf( "((%s)lapg_m[lapg_head-%i].sym)", gr.sym[i].type, length );
					else if( i==-1 && gr.sym[gr.rleft[rule]].type )
						printf( "((%s)lapg_m[lapg_head-%i].sym)", gr.sym[gr.rleft[rule]].type, length );
					else
						printf( "lapg_m[lapg_head-%i].sym", length );

				} else error( 0, "in rule, defined at line %i:\n\twarning: %s has no attributes, $# skipped\n", gr.rlines[rule], gr.sym[gr.rleft[rule]].name );

			} else if( *l >= '0' && *l <= '9' ) {

				for( i = 0; *l >= '0' && *l <= '9'; l++ ) i = i * 10 + (*l - '0');

				if( i >= length ) 
					error( 0, "in rule, defined at line %i:\n\telement $%i is absent, skipped\n", gr.rlines[rule], i );
				else printf( "lapg_m[lapg_head-%i].sym", length-i-1 );

			} else if( *l>='a' && *l<='z' || *l>='A' && *l<='Z' || *l=='_' ) {

				p = l;
				while( *l>='a' && *l<='z' || *l>='A' && *l<='Z' || *l>='0' && *l<='9' || *l=='_' ) l++;
				m = l;
				i = 0;
				if( *l == '#' && l[1] >= '0' && l[1] <= '9' )
					for( l++; *l >= '0' && *l <= '9'; l++ ) i = i * 10 + (*l - '0');

				c = *m; *m = 0; num = i++;

				if( !strcmp( gr.sym[gr.rleft[rule]].name, p ) ) {
					i--; e = 0; k = gr.rleft[rule];
				}
				if( i ) for( e = 1, rl = gr.rright+rpos; e <= length; rl++, e++ )
					if( !strcmp( gr.sym[*rl].name, p ) ) {
						k = *rl;
						if( --i == 0 ) break;
					}

				if( !i ) {
					p = gr.sym[k].type;

					if( p && !e && expand_cpp )
						printf( "*(%s *)&lapg_gg.sym", p );
					else {
						if( p )
							printf( "((%s)", p );

						if( e )
							printf( "lapg_m[lapg_head-%i].sym", length-e );
						else
							printf( "lapg_gg.sym" );

						if( p )
							printf( ")" );
					}

				} else {
					error( 0, "in rule, defined at line %i:\n\tidentifier $%s#%i was not found, skipped\n", gr.rlines[rule], p, num );
				}
				*m = c;

			} else {
				error( 0, "in rule, defined at line %i:\n\tthe $ sign is skipped\n", gr.rlines[rule] );
			}

		} else if( *l == '@' ) {
			int endpos = 0;
			l++;

			if( *l == '~' ) {
				l++;
				endpos = 1;
			}

			if( *l == '$' ) {
				printf( endpos ? "lapg_gg.endpos" : "lapg_gg.pos" );
				l++;

			} else if( *l >= '0' && *l <= '9' ) {

				for( i = 0; *l >= '0' && *l <= '9'; l++ ) i = i * 10 + (*l - '0');

				if( i >= length )
					error( 0, "in rule, defined at line %i:\n\telement @%i is absent, skipped\n", gr.rlines[rule], i );
				else 
					printf( "lapg_m[lapg_head-%i].%spos", length-i-1, endpos ? "end" : "" );
			} else
				error( 0, "in rule, defined at line %i:\n\tthe @ sign is skipped\n", gr.rlines[rule] );

		} else {
			p = l;
			while( *l && *l != '$' && *l != '@' && *l != '\n' ) l++;
			c = *l; *l = 0; 
			printf( "%s", p );
			*l = c;
		}
	}
}


//	writes preprocessed semantic action for lexem to file
void SourceGenerator::print_lexem_action( char *action, char *type, int expand_at, int tabcount )
{
	int i;
	char *p, *l = action;
	char c;

	if( !langs[language].addLineInfo ) {	/* skip #line */
		while( *l && strncmp(l,"#line",5) == 0 ) {
			while( *l && *l != '\n' ) l++;
			if( *l == '\n' ) l++;
		}
	}

	if( tabcount > 10 ) tabcount = 10;
	while( *l ) {
		if( *l == '\n' ){

			if( !langs[language].addLineInfo && strncmp(l+1,"#line",5) == 0 ) {
				l++;
				while( *l && *l != '\n' ) l++;
			} else {
				printf( "\n%s", tabs + 10 - tabcount );
				l++;
			}

		} else if( *l == '$' ) {
			l++;

			if( *l == '$' )
				printf( "lapg_n.lexem" ), l++;

			else if( *l == '@' )
				printf( "lapg_n.pos" ), l++;
			
			else if( *l>='a' && *l<='z' || *l>='A' && *l<='Z' || *l=='_' ) {
				p = l;
				while( *l>='a' && *l<='z' || *l>='A' && *l<='Z' || *l>='0' && *l<='9' || *l=='_' ) l++;
				c = *l; *l = 0;
				for( i = 0; i < gr.nsyms; i++ )
					if( !strcmp( gr.sym[i].name, p ) ) break;

				if( i == gr.nsyms )
					error( 0, "in lexem action: $%s symbol is unknown, skipped\n", p );
				else 
					printf( "%i", i );
				*l = c;

			} else
				error( 0, "in lexem action: the $ sign is skipped\n" );
			
		} else if( *l == '@' ) {
			l++;

			if( *l >= '0' && *l <= '9' ) {
				for( i = 0; *l >= '0' && *l <= '9'; l++ ) i = i * 10 + (*l - '0');

				if( i >= BITS || lr.groupset[i] == -1 ) 
					error( 0, "in lexem action: @%i group was not found, skipped\n", i );
				else printf( "%i", lr.groupset[i] );
			} else {
				if( type && expand_at )
					printf( "*(%s *)&lapg_n.sym", type );
				else 
					printf( "lapg_n.sym" );
			}

		} else {
			p = l;
			while( *l && *l != '$' && *l != '@' && *l != '\n' ) l++;
			c = *l; *l = 0; 
			printf( "%s", p );
			*l = c;
		}
	}
}


// print part of code
void SourceGenerator::print_code( bool last, int tabs )
{
	enum { SIZE = 16384, LINE = 4096 };

	char *buffer = new char[SIZE], *maxline = buffer + SIZE;
	char *p = buffer, *lastline = buffer;
	int i;

	tabs = (tabs <= 16 && tabs >= 0 ) ? tabs : 0 ;

	while( l < end ) {
		if( *l == '\n' ) {
			if( !last && p - lastline == 2 && lastline[0] == '%' && lastline[1] == '%' ) {
				p = lastline; l++;
				break;
			}

			if( maxline - p <= 2*LINE ) {
				fwrite( buffer, 1, p - buffer, stdout );
				p = lastline = buffer;
			}

			*p++ = *l++;
			for( i = tabs; i; i-- )
				*p++ = '\t';
			lastline = p;

		} else if( *l != '\r' ) {
			*p++ = *l++;
			if( p - lastline >= LINE ) {
				fwrite( buffer, 1, p - buffer, stdout );
				p = lastline = buffer;
			}
		} else {
			l++; // skip CR
		}

		if( l == end )
			fillb();
	}

	if( p == lastline && p - buffer > tabs && p[-tabs-1] == '\n' )
		p -= tabs;
	else
		*p++ = '\n';

	if( p - buffer )
		fwrite( buffer, 1, p - buffer, stdout );

	delete[] buffer;
}

/***********************************************************************************/

void SourceGenerator::printout()
{
	const char *p, *l = langs[language].templ_gen;
	int action[16], deep = 0, tabs = 0;
	const char *loop[16];
	int can_write = 1, denied_at = -1, line = 1;
	char var[128];

	iteration_counter = -2;

	while( *l ) {
		for( p = l; *l && *l != '@' && *l != '$'; l++ ) {
			if( *l == '\n' ) tabs = 0, line++;
			if( *l == '\t' ) tabs++;
		}
		if( l - p && can_write )
			fwrite( p, 1, l - p, stdout );

		if( *l == '@' ) {
			
			for( p = ++l; *l >= '0' && *l <= '9' || *l >= 'a' && *l <= 'z' || *l >= 'A' && *l <= 'Z' || *l == '_'; l++ );
			if( l - p > 120 || l == p ) {
				error( 0, "output_script(%i): @ variable name is wrong\n", line );
				return;
			}
			strncpy( var, p, l - p );
			var[l-p] = 0;
			if( print_variable( var, tabs, can_write ) ) {
				for( ;*l && *l != '\n'; l++ );
				if( *l == '\n' ) l++, line++;
			}

		} else if( *l == '$' ) {
			l++;
			if( *l == '}' ) {
				l++;
				if( !deep ) {
					error( 0, "output_script(%i): unexpected $}\n", line );
					return;
				}

				if( action[deep-1] >= 2 )
					if( update_vars( action[deep-1]-2 ) ) {
						l = loop[deep-1];
						continue;
					} else iteration_counter = -2;

				for( ;*l && *l != '\n'; l++ );
				if( *l == '\n' ) l++, line++;
				deep--;
				if( deep == denied_at ) {
					can_write = 1;
					denied_at = -1;
				}

			} else if( *l == '{' ) {
				if( deep == 16 ) {
					error( 0, "output_script(%i): too deep\n", line );
					return;
				}
				for( p = ++l; *l >= '0' && *l <= '9' || *l >= 'a' && *l <= 'z' || *l >= 'A' && *l <= 'Z' || *l == '_'; l++ );
				if( l - p > 120 || l == p ) {
					error( 0, "output_script(%i): ${ variable name is wrong\n", line );
					return;
				}
				strncpy( var, p, l - p );
				var[l-p] = 0;
				action[deep] = check_variable(var);
				if( action[deep] == 0 && can_write ) {
					can_write = 0;
					denied_at = deep;
				}
				for( ;*l && *l != '\n'; l++ );
				if( *l == '\n' ) l++, line++;
				loop[deep] = l;
				deep++;

			} else if( *l == '#' ) {
				for( p = ++l; *l >= '0' && *l <= '9' || *l >= 'a' && *l <= 'z' || *l >= 'A' && *l <= 'Z' || *l == '_'; l++ );
				if( l - p > 120 || l == p ) {
					error( 0, "output_script(%i): $# variable name is wrong\n", line );
					return;
				}
				strncpy( var, p, l - p );
				var[l-p] = 0;
				int i = check_variable(var);
				if( i>=2 ) {
					error( 0, "output_script(%i): $# variable cannot be used in loop\n", line );
					return;
				}
				if( !i ) {
					for( ;*l && *l != '\n'; l++ );
					if( *l == '\n' ) l++, line++;
				}

			} else printf( "$" );
		}
	}
	if( deep )
		error( 0, "output_script(%i): unclosing $} is absent\n", line );
}


static const char *at_varlist[] = {
	"target",		/* 0 */
	"lstates",
	"lchars",		/* 2 */
	"nstates",
	"next",
	"nactions", 	/* 5 */
	"nsyms",
	"gotosize", 	/* 7 */
	"rules",
	"classname",
	"errprefix",	/* 10 */
	"error",
	"maxtoken", 	/* 12 */
	"maxstack",
	"char2no",
	"lexem",		/* 15 */
	"action",
	"lalr", 		/* 17 */
	"sym_goto",
	"sym_from",
	"sym_to",		/* 20 */
	"rlen",
	"rlex", 		/* 22 */
	"syms",
	"lexemnum",
	"lexemactioncpp",/* 25 */
	"lexemaction",
	"rulenum",		/* 27 */
	"ruleactioncpp",
	"ruleaction",
	"nativecode",	/* 30 */
	"nativecodeall",
	"namespace",	/* 32 */
	"tokenenum",
	NULL
};


static const char *sym_to_string( const char *s, int number ) {
	static char buffer[4096];

	*buffer = 0;
	if( s[0] == '\'' ) {
		const char *p = s+1;
		char *dest = buffer;
		while( *p && *(p+1) ) {
			if( *p >= 'a' && *p <= 'z' || *p >= 'A' && *p <= 'Z' || *p == '_' )
				*dest++ = *p++;
			else {
				char *name = NULL;
				switch( *p ) {
					case '{': name = "LBRACE"; break;
					case '}': name = "RBRACE"; break;
					case '[': name = "LBRACKET"; break;
					case ']': name = "RBRACKET"; break;
					case '(': name = "LROUNDBRACKET"; break;
					case ')': name = "RROUNDBRACKET"; break;
					case '.': name = "DOT"; break;
					case ',': name = "COMMA"; break;
					case ':': name = "COLON"; break;
					case ';': name = "SEMICOLON"; break;
					case '+': name = "PLUS"; break;
					case '-': name = "MINUS"; break;
					case '*': name = "MULT"; break;
					case '/': name = "DIV"; break;
					case '%': name = "PERC"; break;
					case '&': name = "AMP"; break;
					case '|': name = "OR"; break;
					case '^': name = "XOR"; break;
					case '!': name = "EXCL"; break;
					case '~': name = "TILDE"; break;
					case '=': name = "EQ"; break;
					case '<': name = "LESS"; break;
					case '>': name = "GREATER"; break;
					case '?': name = "QUESTMARK"; break;
				}
				if( name )
					while( *name ) *dest++ = *name++;
				else {
					sprintf( dest, "N%02X", *p );
					while( *dest ) dest++;
				}
				p++;
			}
		}
		*dest = 0;
		return buffer;

	} else if( s[0] == '{' && s[1] == '}' ) {
		sprintf(buffer,"_sym%i",number);
		return buffer;
	} else {
		return s;
	}
}


int SourceGenerator::print_variable( char *var, int tabs, int can_write )
{
	int i, e;

	for( i = 0; at_varlist[i]; i++ )
		if( !strcmp( var, at_varlist[i] ) )
			break;

	if( !at_varlist[i] ) {
		error( 0, "output_script: unknown @ variable: %s\n", var );
		return 0;
	}

	if( !can_write )
		return 0;

	switch( i ) {
		case 0:  /* target */	 printf( "%s", targetname ); break;
		case 1:  /* lstates */	 printf( "%i", lr.nstates ); break;
		case 2:  /* lchars */	 printf( "%i", lr.nchars ); break;
		case 3:  /* nstates */	 printf( "%i", gr.nstates ); break;
		case 4:  /* next */ 	 printf( "%s", (getsym)?getsym:"next()" ); break;
		case 5:  /* nactions */  printf( "%i", gr.nactions ); break;
		case 6:  /* nsyms */	 printf( "%i", gr.nsyms ); break;
		case 7:  /* gotosize */  printf( "%i", gr.sym_goto[gr.nsyms] ); break;
		case 8:  /* rules */	 printf( "%i", gr.rules ); break;
		case 9:  /* classname */ printf( "%s", (classn)?classn:"parser" );break;
		case 10: /* errprefix */ if( errprefix ) printf( "%s", errprefix ); break;
		case 11: /* error */	 printf( "%i", gr.errorn ); break;
		case 12: /* maxtoken */  printf( "%i", maxtoken ); break;
		case 13: /* maxstack */  printf( "%i", maxstack ); break;
		case 14: /* char2no */	 
			for( i = 0; i < 256; ) {
				printf( " %3i,", lr.char2no[i] );
				if( ++i % 16 == 0 && i < 256 ) printf( "\n%s", TABS(tabs) );
			}
			printf( "\n" );
			return 1;	
		case 15: /* lexem */  
			for( i = 0; i < lr.nstates; i++ ) {
				if( i ) printf( "%s%s", TABS(tabs),  langs[language].lexem_start);
					else printf(langs[language].lexem_start );
				for( e = 0; e < lr.nchars; e++ )
					printf( "%4i,", lr.dta[i]->change[e] );
				printf( " %s,\n", langs[language].lexem_end );
			}
			return 1;
		case 16: /* action */
			for( i = 0; i < gr.nstates; i++ ) {
				if( i && !(i%16) ) printf( "\n%s", TABS(tabs) );
				printf( "%4i,", gr.action_index[i] );
			}
			printf( "\n" );
			return 1;
		case 17: /* lalr */
			for( i = 0; i < gr.nactions; i++ ) {
				if( i && !(i%16) ) printf( "\n%s", TABS(tabs) );
				printf( "%4i,", gr.action_table[i] );
			}
			printf( "\n" );
			return 1;
		case 18: /* sym_goto */
			for( i = 0; i <= gr.nsyms; i++ ) {
				if( i && !(i%16) ) printf( "\n%s", TABS(tabs) );
				printf( "%4i,", gr.sym_goto[i] );				
			}
			printf( "\n" );
			return 1;
		case 19: /* sym_from */
			for( i = 0; i < gr.sym_goto[gr.nsyms]; i++ ) {
				if( i && !(i%16) ) printf( "\n%s", TABS(tabs) );
				printf( "%4i,", gr.sym_from[i] );				
			}
			printf( "\n" );
			return 1;
		case 20: /* sym_to */
			for( i = 0; i < gr.sym_goto[gr.nsyms]; i++ ) {
				if( i && !(i%16) ) printf( "\n%s", TABS(tabs) );
				printf( "%4i,", gr.sym_to[i] );				
			}
			printf( "\n" );
			return 1;
		case 21: /* rlen */
			for( i = 0; i < gr.rules; i++ ) {
				if( i && !(i%16) ) printf( "\n%s", TABS(tabs) );
				for( e = 0; gr.rright[ gr.rindex[i]+e ] >= 0; e++ );
				printf( "%4i,", e );
			}
			printf( "\n" );
			return 1;
		case 22: /* rlex */
			for( i = 0; i < gr.rules; i++ ) {
				if( i && !(i%16) ) printf( "\n%s", TABS(tabs) );
				printf( "%4i,", gr.rleft[i] );
			}
			printf( "\n" );
			return 1;
		case 23: /* syms */
			for( i = 0; i < gr.nsyms; i++ )
				printf( "%s\"%s\",\n", i?TABS(tabs):"", gr.sym[i].name );
			return 1;
		case 24: /* lexemnum */
			if( iteration_counter >= 0 && iteration_counter < lr.nterms )
				printf( "%i", lr.lnum[iteration_counter] );
			else
				error( 0, "internal: using @%s in wrong loop\n", var );
			break;
		case 25: /* lexemactioncpp */
			if( iteration_counter >= 0 && iteration_counter < lr.nterms )
				print_lexem_action( lr.lact[iteration_counter], gr.sym[lr.lnum[iteration_counter]].type, 1, tabs );
			else
				error( 0, "internal: using @%s in wrong loop\n", var );
			break;
		case 26: /* lexemaction */
			if( iteration_counter >= 0 && iteration_counter < lr.nterms )
				print_lexem_action( lr.lact[iteration_counter], gr.sym[lr.lnum[iteration_counter]].type, 0, tabs );
			else
				error( 0, "internal: using @%s in wrong loop\n", var );
			break;
		case 27: /* rulenum */
			if( iteration_counter >= 0 && iteration_counter < gr.rules )
				printf( "%i", iteration_counter );
			else
				error( 0, "internal: using @%s in wrong loop\n", var );
			break;
		case 28: /* ruleactioncpp */
			if( iteration_counter >= 0 && iteration_counter < gr.rules )
				print_action( gr.raction[iteration_counter], iteration_counter, 1, tabs );
			else
				error( 0, "internal: using @%s in wrong loop\n", var );
			break;
		case 29: /* ruleaction */
			if( iteration_counter >= 0 && iteration_counter < gr.rules )
				print_action( gr.raction[iteration_counter], iteration_counter, 0, tabs );
			else
				error( 0, "internal: using @%s in wrong loop\n", var );
			break;
		case 30: /* nativecode */
			print_code( false, tabs );
			return 1;
		case 31: /* nativecodeall */
			print_code( true, tabs );
			return 1;
		case 32: /* namespace */ 
			printf( "%s", (ns)?ns:"lapg" );
			break;
		case 33: /* tokenenum */
			for( i = 0; i < gr.nsyms; i++ )
				printf( "%s%s,\n", i?TABS(tabs):"", sym_to_string(gr.sym[i].name, i) );
			return 1;
			break;
	}
	return 0;
}

static const char *dollar_varlist[] = {
	"pos",			/* 0 */
	"pos0",
	"pos1", 		/* 2 */
	"pos2",
	"noterror",
	"error",		/* 5 */
	"nactions",
	"lexemactions", /* 7 */
	"ruleactions",
	"eachlexem",
	"eachaction",	/* 10 */
	"lexemend",
	"pos3",			/* 12 */
	"breaks",
	NULL
};

// returns: 0:deny, 1:grant, 2...:loop
int SourceGenerator::check_variable( char *var )
{
	int i;
	
	for( i = 0; dollar_varlist[i]; i++ )
		if( !strcmp( var, dollar_varlist[i] ) )
			break;
		
		if( !dollar_varlist[i] ) {
			error( 0, "internal: unknown $ variable: %s\n", var );
			return 0;
		}
		
		switch( i ) {
		case 0:  /* pos */			return positioning!=0;
		case 1:  /* pos0 */ 		return positioning==0;
		case 2:  /* pos1 */ 		return positioning==1;
		case 3:  /* pos2 */ 		return positioning==2;
		case 4:  /* noterror */ 	return gr.errorn==-1;
		case 5:  /* error */		return gr.errorn!=-1;
		case 6:  /* nactions */ 	return gr.nactions!=0;
		case 7:  /* lexemactions */
			for( i = 0; i < lr.nterms; i++ )
				if( lr.lact[i] ) return 1;
			return 0;
		case 8:  /* ruleactions */
			for( i = 0; i < gr.rules; i++ )
				if( gr.raction[i] ) return 1;
			return 0;
		case 9:  /* eachlexem */
			if( iteration_counter != -2 ) {
				error( 0, "output_script: using nested loops\n" );
				return 0;
			}
			iteration_counter = -1;
			update_vars( 0 );
			return 2;
		case 10: /* eachaction */
			if( iteration_counter != -2 ) {
				error( 0, "output_script: using nested loops\n" );
				return 0;
			}
			iteration_counter = -1;
			update_vars( 1 );
			return 3;
		case 11: /* lexemend */
			return (lexemend && positioning) ? 1 : 0;
		case 12:  /* pos3 */ 		return positioning==3;
		case 13:  /* breaks */      return genbreaks ? 1 : 0;
	}
	return 0;
}

int SourceGenerator::update_vars( int type )
{
	if( iteration_counter == -2 ) {
		error( 0, "output_script: update vars failed\n" );
		return 0;
	}

	iteration_counter++;
	switch( type ) {
	case 0:
		for( ; iteration_counter < lr.nterms && !lr.lact[iteration_counter]; iteration_counter++ );
		return iteration_counter < lr.nterms;
	case 1:
		for( ; iteration_counter < gr.rules && !gr.raction[iteration_counter]; iteration_counter++ );
		return iteration_counter < gr.rules;
	}
	return 0;
}

// Templates

void SourceGenerator::TemplateFromString()
{
	const char *s = langs[language==-1?0:language].templ_string;
	if( s ) 
		fwrite( s, 1, strlen(s), stdout );
	else
		fprintf( stderr, "template-from-string absent for %s language\n", langs[language==-1?0:language].lang );
}

void SourceGenerator::TemplateFromFile()
{
	const char *s = langs[language==-1?0:language].templ_file;
	if( s ) 
		fwrite( s, 1, strlen(s), stdout );
	else
		fprintf( stderr, "template-from-file absent for %s language\n", langs[language==-1?0:language].lang );
}

void SourceGenerator::printScript()
{
	const char *s = langs[language==-1?0:language].templ_gen;
	fwrite( s, 1, strlen(s), stdout );
}
