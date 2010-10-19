// main.h

#ifndef main_h_included
#define main_h_included

#define DEBUG_syntax

#include <stdlib.h>
#include <stdio.h>
#include <stdarg.h>

class parser {
private:
	char b[1025], *l, *end;
	void error( char *r, ... );

public:
	int parse();
	void fillb();
};

#endif
