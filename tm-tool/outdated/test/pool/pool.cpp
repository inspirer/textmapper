// cl /nologo /Zi /MDd /D"_DEBUG" pool.cpp

#include <stdlib.h>
#include <stdio.h>
#include <crtdbg.h>

#include "pool.h"

int YourReportHook( int reportType, char *message, int *returnValue ) {
	printf("%s\n", message);
	return 1;
}

void test() {
	Pool p;
	Array<int> array(&p);
	array[0] = 1;
	array[1] = 2;
	array[2] = 3;
	printf("%i %i %i\n", array[0], array[1], array[2] );

	//Array<int> arr2 = array;
	//Array<int> arr3(array);
	Array<int> arr2(&p);
	//arr2 = array;
}

int main(int argc,char *argv[]) {
	test();
	_CrtSetReportHook(YourReportHook);
	printf("leaks: %i", _CrtDumpMemoryLeaks());
}
