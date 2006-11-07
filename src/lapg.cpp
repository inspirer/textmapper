/*   lapg.cpp
 *
 *   Lapg (Lexical Analyzer and Parser Generator)
 *   Copyright (C) 2002-06  Evgeny Gryaznov (inspirer@inbox.ru)
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
#include "srcgen.h"

#define VERSION "1.2.3"
#define BUILD   __DATE__

static char help_message[] =
"lapg - Lexical analyzer and parser generator\n"
"Evgeny Gryaznov, 2002-06, inspirer@inbox.ru\n"
"\n"
"usage: lapg [OPTIONS]... [inputfile [outputfile]]\n"
"\n"
"Generator:\n"
"  -d,  --debug                   debug info\n"
"  -e,  --extended-debug          extended debug info\n"
"\n"
"Operations:\n"
"  -h,  --help                    display this help\n"
"  -v,  --version                 output version information\n"
"  -tf, --template-from-file      generate template for file parsing\n"
"  -ts, --template-from-string    generate template for string parsing\n"
"  -vb, --verbose                 print output script\n"
"  -l name, --lang=name           language (for tf/ts/vb), supported: c++, cs, js, java\n"
"\n"
"Defaults:\n"
"  inputfile = syntax\n"
"  outputfile = parse.cpp/cs/java/js...\n"
"\n"
;

#define check(short_form, long_form) (!strcmp(argv[i],short_form) || !strcmp(argv[i],long_form) )

int main(int argc,char *argv[])
{
	int debug = 0, action = 0, lang = -1;
	char *input = "syntax", *output = NULL;

	for( int i = 1, e = 0; i < argc; i++ )

		if( check( "-d", "--debug" ) ) {
			debug = 1;

		} else if( check( "-e", "--extended-debug" ) ) {
			debug = 2;

		} else if( check( "-h","--help" ) ) {
			printf( help_message );
			return 0;

		} else if( check( "-v","--version" ) ) {
			printf( 
				"lapg v" VERSION " build " BUILD "\n"
				"Evgeny Gryaznov, 2002-06, inspirer@inbox.ru\n" );
			return 0;

		} else if( check( "-tf","--template-from-file" ) ) {
			action = 1;

		} else if( check( "-ts","--template-from-string" ) ) {
			action = 2;

		} else if( check( "-vb","--verbose" ) ) {
			action = 3;

		} else if( !strcmp( argv[i], "-l" ) || !strncmp( argv[i], "--lang=", 7 ) ) {
			char *lng;
			int e;

			if( argv[i][1] == '-' )
				lng = argv[i] + 7;
			else
				lng = argv[++i];

			for( e = 0; langs[e].lang; e++ )
				if( !strcmp( langs[e].lang, lng ) )
					break;

			if( langs[e].lang ) {
				lang = e;
			} else {
				fprintf( stderr, "lapg: unknown language: %s\n", lng );
			}

		} else if( argv[i][0]=='-' && argv[i][1]!=0 ) {
			fprintf(stderr,"lapg: invalid option %s\n",argv[i]);
			fprintf(stderr,"Try 'lapg --help' for more information.\n");
			return 1;

		} else switch( e++ ){
			case 0: input = argv[i]; break;
			case 1: output = argv[i]; break;
		}

	SourceGenerator *g = new SourceGenerator;
	g->language = lang;

	switch( action ) {

		// run
		case 0:

			// open file
			if( *input && input[0]!='-' && input[1]!=0 ) 
				if( !freopen( input, "r", stdin ) ) {
					perror( input );
					return 1;
				}

			// save options
			g->sourcename = (*input && input[0]!='-' && input[1]!=0) ? input : "<stdin>";
			g->targetname = output;

			unlink("errors");unlink("states");
			g->process( debug );
			break;

		// --template-from-file
		case 1:
			g->TemplateFromFile();
			break;

		// --template-from-string
		case 2:
			g->TemplateFromString();
			break;

		// --verbose
		case 3:
			g->printScript();
			break;
	}

	delete g;

	#ifdef MEM_DEBUG
		_CrtDumpMemoryLeaks();
	#endif
	return 0;
}
