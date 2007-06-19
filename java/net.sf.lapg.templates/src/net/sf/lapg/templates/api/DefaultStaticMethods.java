package net.sf.lapg.templates.api;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class DefaultStaticMethods implements IStaticMethods {
	
	private static Set<String> supported = getSupported();
	
	private static HashSet<String> getSupported() {
		HashSet<String> set = new HashSet<String>();
		Collections.addAll(set, new String[] { "print" });
		return set;
	}

	public boolean isSupported(String name) {
		return supported.contains(name);
	}
	
	public String print(Object[] list, String separator, Integer maxwidth) {
		StringBuffer sb = new StringBuffer();
		int i = 0, lineStart = 0;
		for( Object a : list ) {
			if( i > 0 )
				sb.append(separator);
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
