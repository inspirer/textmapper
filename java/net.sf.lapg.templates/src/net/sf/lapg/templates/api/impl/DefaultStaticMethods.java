package net.sf.lapg.templates.api.impl;


public class DefaultStaticMethods {

	public String print(Object[] list, String separator, Integer maxwidth) {
		StringBuffer sb = new StringBuffer();
		int i = 0, lineStart = 0;
		for( Object a : list ) {
			if( i > 0 ) {
				sb.append(separator);
			}
			String str = a.toString();
			if( sb.length() + str.length() - lineStart >= maxwidth) {
				sb.append('\n');
				lineStart = sb.length();
			}
			sb.append(str);
			i++;
		}
		return sb.toString();
	}
}
