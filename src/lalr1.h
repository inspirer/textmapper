/*   lalr1.h
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

#ifndef lalr1_h_included
#define lalr1_h_included

//#define DEBUG_syntax

#include "common.h"
#include "gbuild.h"
#include "lbuild.h"


class lalr1 : public IError {
public:
	const char *sourcename;

private:
	GrammarBuilder gb;
	LexicalBuilder lb;
	int length, rule[128];
	int parse();

protected:
	GrammarBuilder::Result gr; 
	LexicalBuilder::Result lr;
	int debuglev;
	int maxtoken, maxstack;
	char *l, *end;

	virtual void process_directive( char *id, int value, int line, int column );
	virtual void process_directive( char *id, char *value, int line, int column );
	virtual void fillb()=0;

	int run();
	void clear();
	virtual ~lalr1(){};
};

#endif
