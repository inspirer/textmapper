// main.cpp

#include "main.h"

void parser::error( char *r, ... )
{
	va_list arglist;
	va_start( arglist, r );
	vfprintf( stderr, r, arglist );
}


void parser::fillb()
{
	int size = fread( b, 1, 1024, stdin );
	b[size] = 0; end = b + size; l = b;
}


int main( int argc, char *argv[] )
{
	int  i;
	char *input = "-";
	parser p;
	
	for( i = 1; i < argc; i++ ) {
		if( argv[i][0]!='-' || argv[i][1]==0 )
			input = argv[i];
	}

	if( input[0] != '-' || input[1] != 0 )
		if( !freopen( input, "r", stdin ) ) {
			perror( input );
			return 1;
		}

	p.fillb();
	p.parse();
	return 0;
}
