/*   srcgen.h
 *
 *   Lapg (Lexical Analyzer and Parser Generator)
 *   Copyright (C) 2002-03  Eugeniy Gryaznov (gryaznov@front.ru)
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

#ifndef srcgen_h_included
#define srcgen_h_included

#include "common.h"
#include "lalr1.h"

using std::vector;

struct language {
	const char *lang;
	const char *templ_gen;
	const char *templ_string;
	const char *templ_file;
	const char *output;
};

extern const language langs[];

// common, language independent actions
class SourceGenerator : public lalr1 {
private:
	char b[1025];
	FILE *err, *dbg;

protected:
	void fillb();

	int positioning;
	char *classn, *getsym, *errprefix, *ns;

	void printout();

	void error( int e, char *r, ... );
	void print_action( char *action, int rule, int expand_cpp, int tabcount );
	void print_lexem_action( char *action, char *type, int expand_at, int tabcount );
	void process_directive( char *id, char *value, int line, int column );

	int iteration_counter;

	void print_code( bool last, int tabs );
	void print_lexem_array();
	int  print_variable( char *var, int tabs, int can_write );
	int  check_variable( char *var );
	int  update_vars( int type );

public:
	const char *targetname;
	int language;

	void process( int debug );
	void TemplateFromString();
	void TemplateFromFile();
	void printScript();
};

#endif
