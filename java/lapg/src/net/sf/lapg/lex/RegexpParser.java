package net.sf.lapg.lex;

import java.util.ArrayList;
import java.util.Arrays;

import net.sf.lapg.IError;

public class RegexpParser {
	
	private IError err;
	
	private int[] symbols;
	private int[] sym;
	private int[] stack;
	private int[] set;
	
	public RegexpParser(IError err) {
		this.err = err;
		this.symbols = new int[LexConstants.SIZE_SYM];
		this.sym = new int[LexConstants.MAX_ENTRIES];
		this.stack = new int[LexConstants.MAX_DEEP];
		this.set = new int[LexConstants.SIZE_SYM];
		this.setpools = new ArrayList<int[]>();
	}

	private int index;
	private char[] re;
	private String regexp;
	private ArrayList<int[]> setpools;

	private int escape(int c) {
		switch (c) {
		case 'a':
			return 7;
		case 'b':
			return '\b';
		case 'f':
			return '\f';
		case 'n':
			return '\n';
		case 'r':
			return '\r';
		case 't':
			return '\t';
		default:
			return c;
		}
	}

	private int getnext() {
		int i, e;
		boolean invert = false;

		switch( re[index] ) {
			case '(': 
				index++; 
				return LexConstants.LBR;

			case ')': 
				index++; 
				return LexConstants.RBR;

			case '|': 
				index++; 
				return LexConstants.OR;

			case '.':
				index++; 
				symbols[('\n')/LexConstants.BITS] |= (1<<(('\n')%LexConstants.BITS));
				return LexConstants.ANY;

			case '[':
				index++;
				invert = index < re.length && re[index] == '^';
				if( invert ) { 
					index++; 
				}

				Arrays.fill( set, 0);
				
				while( index < re.length && re[index] != ']' ) {
					if( (i = re[index]) == '\\' ) {
						index++;
						if( index >= re.length ) {
							err.error( "lex: \\ found at the end of expression: /"+regexp+"/\n" );
							return -1;
						}
						i = escape( re[index] );
					}
					set[(i)/LexConstants.BITS] |= (1<<((i)%LexConstants.BITS));
					index++;
					if( index+1 < re.length && re[index] == '-' ) {
						index++;
						e = re[index];
						if( e != ']' ) {
							if( e == '\\' ) {
								index++;
								if( index >= re.length ) {
									err.error( "lex: \\ found at the end of expression: /"+regexp+"/\n" );
									return -1;
								}
								e = escape( re[index] );
							}

							if( e > i )
								for( i++; i <= e; i++ )
									set[(i)/LexConstants.BITS] |= (1<<((i)%LexConstants.BITS));
							else 
								for( i--; i >= e; i-- )
									set[(i)/LexConstants.BITS] |= (1<<((i)%LexConstants.BITS));

							index++;
						} else {
							index--;
						}
					}
				}

				if( index >= re.length ) {
					err.error( "lex: enclosing square bracket not found:" );
					return -1;
				}

				for( i = 0; i < LexConstants.SIZE_SYM; i++ )
					symbols[i] |= set[i];

				if( invert ) {
					for( i = 0; i < LexConstants.SIZE_SYM; i++ )
						set[i] = ~set[i];
					set[(0)/LexConstants.BITS] &= ~(1<<((0)%LexConstants.BITS));
				}

				int[] n = new int[LexConstants.SIZE_SYM];
				for( i = 0; i < LexConstants.SIZE_SYM; i++ )
					n[i] = set[i];
				int res = setpools.size() * LexConstants.SIZE_SYM;
				setpools.add(n);

				index++;
				return res;

			case '\\':
				index++;
				if( index >= re.length ) {
					err.error( "lex: \\ found at the end of expression: /"+regexp+"/\n" );
					return -1;
				}
				i = escape( re[index] );
				symbols[(i)/LexConstants.BITS] |= (1<<((i)%LexConstants.BITS));
				index++; 
				return i|LexConstants.SYM;

			default:
				i = re[index];
				symbols[(i)/LexConstants.BITS] |= (1<<((i)%LexConstants.BITS));
				index++;
				return i|LexConstants.SYM;
		}
	}
	
	public Result compile(int number, String name, String expressionParam) {
		
		int length = 0, e = 1;
		boolean addbrackets = false;

		this.index = 0;
		this.regexp = expressionParam;
		this.re = this.regexp.toCharArray();
		
		Result result = new Result();

		if( re.length == 0 ) {
			err.error( "lex: regexp for `"+name+"' does not contain symbols\n" );
			return null;
		}
		
		while( index < re.length ) {
			if( length > LexConstants.MAX_ENTRIES-5 ) {
				err.error( "lex: regular expression is too long: /"+regexp+"/\n" );
				return null;
			}

			sym[length] = getnext();

			switch( sym[length] ) {
				case LexConstants.LBR:
					stack[e] = length;
					if( ++e >= LexConstants.MAX_DEEP ) {
						err.error( "lex: regular expression is too deep: /"+regexp+"/\n" );
						return null;
					}
					break;
				case LexConstants.OR:
					if( e == 1 ) {
						addbrackets = true; 
					}
					break;
				case LexConstants.RBR:
					if( --e == 0 ) {
						err.error( "lex: error in using parantheses: /"+regexp+"/\n");
						return null;
					}
					sym[stack[e]] |= length;
					/* FALLTHROUGH */
				default:
					if( re[index] == '+' || re[index] == '?' || re[index] == '*' ) {
						switch( re[index] ) {
							case '+': sym[length] |= 1<<29; break;
							case '*': sym[length] |= 2<<29; break;
							case '?': sym[length] |= 3<<29; break;
						}
						if( re[index] != '?' ) 
							sym[++length] = LexConstants.SPL;
						index++;
					}
					break;
				case -1:
					return null;
			}
			length++;
		}

		if( e != 1 ) { 
			err.error( "lex: error in using parantheses: /"+regexp+"/\n");
			return null;
		}

		if( addbrackets ) {
			for( e = 0; e < length; e++ ) { 
				if( (sym[e]&LexConstants.MASK) == LexConstants.LBR ) {
					sym[e] = (sym[e]&~0xffff) | ((sym[e]&0xffff)+1);
				}
			}

			sym[length++] = LexConstants.RBR;
		}

		sym[length++] = -1-number;
		
		return result;
	}
	
	public class Result {
		public int[] sym;
		public int[] setpool;
		public int[] symbols;
	}
}
