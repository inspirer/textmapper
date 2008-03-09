package net.sf.lapg.test.cases;

import junit.framework.TestCase;
import net.sf.lapg.lex.JavaArrayArchiver;

import org.junit.Assert;


public class JavaTablesCompression extends TestCase {

	private void checkDecompression(int[][] a) {
		String c = JavaArrayArchiver.emit_table_as_string(a, 5);
		String starts = a.length + "," + a[0].length + ",\n";
		Assert.assertTrue( c.startsWith(starts));
		c = c.substring(starts.length());

		StringBuffer extractedString = new StringBuffer();
		char[] chs = c.toCharArray();
		boolean isstring = false;
		for( int i = 0; i < chs.length; i++ ) {
			if( chs[i] == '"') {
				isstring = !isstring;
				continue;
			}
			if( isstring ) {
				extractedString.append(chs[i]);
			}
		}

		int[][] b = JavaArrayArchiver.unpackFromString(a.length, a[0].length, extractedString.toString());
		for( int i = 0; i < a.length; i++) {
			for( int e = 0; e < a[0].length; e++ ) {
				if( a[i][e] != b[i][e] ) {
					Assert.fail("wrong decompression at "+i+","+e);
				}
			}
		}
	}

	public void testCompression1() {
		checkDecompression(new int[][] {
				{1,2},
				{3,3}
			});
	}

	public void testCompression2() {
		checkDecompression(new int[][] {
				{1,1},
				{1,1}
			});
	}

	public void testCompression3() {
		checkDecompression(new int[][] {
				{0,0},
				{0,1}
			});
	}

	public void testCompression4() {
		checkDecompression(new int[][] {
				{0},
				{0}
			});
	}

	public void testCompression5() {
		checkDecompression(new int[][] {
				{-789}
			});
	}

	public void testCompression6() {
		checkDecompression(new int[][] {
				{1,2,3,4,5,6,7,8},
				{3,4,5,7,8,8,8,8}
			});
	}

	public void testCompressionBig() {
		checkDecompression(new int[][] {
				{1,2,3,4,5,6,7,8,4,4,4,4,4,4,4,4,5,6,2,1,1,1,1,1,1,43,345,345,345,34,34,345,34,345,34533,333},
				{1,2,3,4,5,6,7,8,4,4,4,4,4,4,4,4,5,6,2,1,1,1,1,1,1,43,345,345,345,34,34,345,34,345,34533,333},
				{1,2,3,4,5,6,7,8,4,4,4,4,4,4,4,4,5,6,2,1,1,1,1,1,1,43,345,345,345,34,34,345,34,345,34533,333},
				{1,2,3,4,5,6,7,8,4,4,4,4,4,4,4,4,5,6,2,1,1,1,1,1,1,43,345,345,345,34,34,345,34,345,34533,333},
				{1,2,3,4,5,6,7,8,4,4,4,4,4,4,4,4,5,6,2,1,1,1,1,1,1,43,345,345,345,34,34,345,34,345,34533,333},
				{1,2,3,4,5,6,7,8,4,4,4,4,4,4,4,4,5,6,2,1,1,1,1,1,1,43,345,345,345,34,34,345,34,345,34533,333},
				{1,2,3,4,5,6,7,8,4,4,4,4,4,4,4,4,5,6,2,1,1,1,1,1,1,43,345,345,345,34,34,345,34,345,34533,333},
				{1,2,3,4,5,6,7,8,4,4,4,4,4,4,4,4,5,6,2,1,1,1,1,1,1,43,345,345,345,34,34,345,34,345,34533,333},
				{1,2,3,4,5,6,7,8,4,4,4,4,4,4,4,4,5,6,2,1,1,1,1,1,1,43,345,345,345,34,34,345,34,345,34533,333},
				{1,2,3,4,5,6,7,8,4,4,4,4,4,4,4,4,5,6,2,1,1,1,1,1,1,43,345,345,345,34,34,345,34,345,34533,333},
				{1,2,3,4,5,6,7,8,4,4,4,4,4,4,4,4,5,6,2,1,1,1,1,1,1,43,345,345,345,34,34,345,34,345,34533,333},
				{1,2,3,4,5,6,7,8,4,4,4,4,4,4,4,4,5,6,2,1,1,1,1,1,1,43,345,345,345,34,34,345,34,345,34533,333},
				{1,2,3,4,5,6,7,8,4,4,4,4,4,4,4,4,5,6,2,1,1,1,1,1,1,43,345,345,345,34,34,345,34,345,34533,333},
				{1,2,3,4,5,6,7,8,4,4,4,4,4,4,4,4,5,6,2,1,1,1,1,1,1,43,345,345,345,34,34,345,34,345,34533,333},
				{1,2,3,4,5,6,7,8,4,4,4,4,4,4,4,4,5,6,2,1,1,1,1,1,1,43,345,345,345,34,34,345,34,345,34533,333},
				{1,2,3,4,5,6,7,8,4,4,4,4,4,4,4,4,5,6,2,1,1,1,1,1,1,43,345,345,345,34,34,345,34,345,34533,333},
				{1,2,3,4,5,6,7,8,4,4,4,4,4,4,4,4,5,6,2,1,1,1,1,1,1,43,345,345,345,34,34,345,34,345,34533,333},
				{1,2,3,4,5,6,7,8,4,4,4,4,4,4,4,4,5,6,2,1,1,1,1,1,1,43,345,345,345,34,34,345,34,345,34533,333},
				{1,2,3,4,5,6,7,8,4,4,4,4,4,4,4,4,5,6,2,1,1,1,1,1,1,43,345,345,345,34,34,345,34,345,34533,333},
			});
	}
}
