// txt2c++.cpp

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

const char *gpl =

	"/*   %s\n"
	" *\n"
	" *   Lapg (Lexical Analyzer and Parser Generator)\n"
	" *   Copyright (C) 2002-07  Evgeny Gryaznov (inspirer@inbox.ru)\n"
	" *\n"
	" *   This program is free software; you can redistribute it and/or modify\n"
	" *   it under the terms of the GNU General Public License as published by\n"
	" *   the Free Software Foundation; either version 2 of the License, or\n"
	" *   (at your option) any later version.\n"
	" *\n"
	" *   This program is distributed in the hope that it will be useful,\n"
	" *   but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
	" *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"
	" *   GNU General Public License for more details.\n"
	" *\n"
	" *   You should have received a copy of the GNU General Public License\n"
	" *   along with this program; if not, write to the Free Software\n"
	" *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA\n"
	" */\n\n";

int main( int argc, char *argv[] )
{
	if( argc < 3 ) {
		printf( "use: txt_to_c++ <output> <input1> ...\n" );
	    return 1;
	}

	if( !freopen( argv[1], "w", stdout ) ) {
		printf( "cannot create file %s\n", argv[1] );
		return 1;
	}
	printf( gpl, argv[1] );

	for( int i = 2; i < argc ; i++ ) {

		FILE *inp = fopen( argv[i], "r" );
		if( !inp ) {
			fprintf( stderr, "cannot open file %s\n", argv[i] );
			return 1;
		}

		static char buff1[4097], buff2[8200];
		buff1[4096] = 0;

		printf( "const char *%s =\n", argv[i] );

		while( fgets( buff1, 4096, inp ) ) {
			char *l = buff1, *p = buff2;
			*p++ = '\t'; *p++ = '"';

			while( !strncmp( l, "    ", 4 ) ) {
					l += 4;
					*p++ = '\\';
					*p++ = 't';
			}

			while( *l ) {
				if( *l != '\r' ) {
					if( *l == '\\' || *l == '"' || *l == '\n' || *l == '\t' ) *p++ = '\\';
					if( *l == '\n' ) 
						*p++ = 'n';
					else if( *l == '\t' )
						*p++ = 't';
					else
						*p++ = *l;
				}
				l++;
			}
			*p++ = '"'; *p++ = '\n';
			fwrite( buff2, 1, p - buff2, stdout );
		}
		printf( ";\n\n" );
		fclose( inp );
	}
	return 0;
}

