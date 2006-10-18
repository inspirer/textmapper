/*   common.h
 *
 *   Lapg (Lexical Analyzer and Parser Generator)
 *   Copyright (C) 2002-04  Eugeniy Gryaznov (gryaznov@front.ru)
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

#ifndef common_h_inlcuded
#define common_h_inlcuded

#include <stdlib.h>
#include <string.h>
#include <stdio.h>
#include <stdarg.h>
#include <vector>

#ifndef GCC_COMPILER
 #define MEM_DEBUG
 #define ASSERT(a) _ASSERT(a)
 #ifdef _DEBUG
	#define RUN_IN_DEBUG
 #endif
#else
 #include <errno.h>
 #include <unistd.h>
 #include <assert.h>
 #define _strdup(a) ((a)?strdup(a):NULL)
 #define ASSERT(a) assert(a)
#endif

#ifdef MEM_DEBUG
#include <crtdbg.h>
#endif

char *strstrip( char *p );
char *concat( char *s1, char *s2, const char *file, int line );
void inc_realloc( void **mem, int count, int mult );
unsigned x_realloc( void **mem, unsigned &used, unsigned &size, unsigned space, int mult );

#ifndef interface
#define interface struct
#endif

interface IError {
	virtual void error( int, char *, ... )=0;
};

#ifdef WORD32

#define MAX_WORD 0x7ffffff0
typedef int word;

#else

#define MAX_WORD 0x7ff0
typedef short word;

#endif

enum { BITS = sizeof(int)*8 };

#define SIZE(n) (((n)+BITS-1)/BITS)
#define IS(set,n) ((set)[(n)/BITS]&(1<<((n)%BITS)))
#define SET(set,n) (set)[(n)/BITS] |= (1<<((n)%BITS))
#define CLEAR(set,n) (set)[(n)/BITS] &= ~(1<<((n)%BITS))
#define ZERO(set,n) memset( set, 0, SIZE(n)*sizeof(int) )
#define ZEROSET(set,n) memset( set, 0, n*sizeof(int) )

#endif
