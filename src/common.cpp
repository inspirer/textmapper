/*   common.cpp
 *
 *   Lapg (Lexical Analyzer and Parser Generator)
 *   Copyright (C) 2002-06  Eugeniy Gryaznov (gryaznov@front.ru)
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

char *strstrip(char *p)
{
	int len = strlen(p) - 2;
	char *s = new char[ len + 1 ];
	strncpy( s, p+1, len );
	s[len] = 0;
	return s;
}

char *concat( char *s1, char *s2, const char *file, int line )
{
	int size_to_allocate = ((s1)?strlen(s1):0) + strlen(s2) + 2;
	if( line != -1 ) size_to_allocate += strlen(file) + 40;
	char *s = new char[  size_to_allocate ];

	if( s1 ) {
		if( line != -1 )
			sprintf( s, "%s\n#line %i \"%s\"\n%s", s1, line, file, s2 );
		else 
			sprintf( s, "%s\n%s", s1, s2 );
	} else {
		sprintf( s, "#line %i \"%s\"\n%s", line, file, s2 );
	}
	delete[] s1;
	delete[] s2;
	return s;
}

enum {
	COUNT = 512,
	COUNT_MASK = (COUNT-1)
};

void inc_realloc( void **mem, int count, int mult )
{
	if( count&COUNT_MASK ) return;
	*mem = realloc( *mem, (count+COUNT)*mult );
}

unsigned x_realloc( void **mem, unsigned &used, unsigned &size, unsigned space, int mult )
{
	unsigned ret;

	if( used < size+space ) {
		while( used < size+space ) used += 16384;
		*mem = realloc( *mem, used*mult );
	}
	ret = size;
	size += space;
	return ret;
}
