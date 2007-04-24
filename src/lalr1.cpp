/*   lalr1.cpp
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
#include "lalr1.h"


// default (language independent) directive processing: string value
void lalr1::process_directive( char *id, char *value, int line, int column )
{
	error( 0, "%s, %i(%i) unknown directive %s\n", sourcename, line, column, id );
	delete[] value;
}


// default (language independent) directive processing: integer value
void lalr1::process_directive( char *id, int value, int line, int column )
{
	if( !strcmp( id, "maxtoken" ) ) 
		maxtoken = value;

	else if( !strcmp( id, "stack" ) ) 
		maxstack = value;

	else 
		error( 0, "%s, %i(%i) unknown directive %s\n", sourcename, line, column, id );
}


// run parsing (from l with fillb)
int lalr1::run()
{
	maxstack = maxtoken = 1024;
	lb.init( this, debuglev );
	gb.init( this, debuglev );
	gb.set_eoi( gb.terminal( "eoi" ) );

	int res = parse();

	if( res ) {
		gb.set_input( gb.symbol( "input", 0 ) );
		res = lb.generate( &lr );
		if( res ) {
			res = gb.generate( &gr );
			if( !res ) 
				lb.clear( &lr );
		} else
			gb.rollback();
	} else {
		lb.rollback();
		gb.rollback();
	}

	return res;
}


// cleanup
void lalr1::clear()
{
	lb.clear(&lr);
	gb.clear(&gr);
}
