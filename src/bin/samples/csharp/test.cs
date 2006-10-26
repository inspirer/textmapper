
// (x)y, (x)(y), and (x)(-y) are cast-expressions, 
// but (x)-y is not, even if x identifies a type. 
// if x == int, then all four forms are cast-expressions 

class a { 
  void m() {
	
	// cast
	int e = (x)y;
	int q = (x)(y);
	int p = (x)(-y);
	// not-cast
	int t = (x)-y;
	// cast
	int t2 = (int)-y;
	int t = (x[][,])-y;
  } 
}
