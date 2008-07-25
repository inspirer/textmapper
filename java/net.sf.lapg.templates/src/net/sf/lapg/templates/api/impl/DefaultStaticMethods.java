package net.sf.lapg.templates.api.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class DefaultStaticMethods {

	public String print(ArrayList<?> list, String separator, Integer maxwidth) {
		return print(list.toArray(), separator, maxwidth);
	}

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

	public String print(HashMap<?,?> map) {
		StringBuffer sb = new StringBuffer();
		int i = 0;
		sb.append("[");
		for( Map.Entry<?,?> a : map.entrySet() ) {
			if( i > 0 ) {
				sb.append(",");
			}
			sb.append(a.getKey());
			sb.append(" -> ");
			sb.append(a.getValue());
			i++;
		}
		sb.append("]");
		return sb.toString();
	}
}
