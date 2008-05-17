package net.sf.lapg.common;

public class FormatUtil {

	public static String asHex(int i, int width) {
		String s = Integer.toHexString(i);
		if( s.length() >= width )
			return s;
		StringBuffer sb = new StringBuffer();
		for( int chars = width - s.length(); chars > 0; chars-- ) {
			sb.append('0');
		}
		sb.append(s);
		return sb.toString();
	}

	public static String asDecimal(int i, int width, char padding) {
		String s = Integer.toString(i);
		if( s.length() >= width )
			return s;
		StringBuffer sb = new StringBuffer();
		for( int chars = width - s.length(); chars > 0; chars-- ) {
			sb.append(padding);
		}
		sb.append(s);
		return sb.toString();
	}
}
