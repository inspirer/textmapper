package net.sf.lapg.gen;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.sf.lapg.templates.api.IStaticMethods;

public class TemplateStaticMethods implements IStaticMethods {
	private static Set<String> supported = getSupported();

	private static HashSet<String> getSupported() {
		HashSet<String> set = new HashSet<String>();
		Collections.addAll(set, new String[] { "format" });
		return set;
	}

	public boolean isSupported(String name) {
		return supported.contains(name);
	}

	public static String format(short[] table, int maxwidth, int leftpadding ) {
		StringBuffer sb = new StringBuffer();
		for( int i = 0; i < table.length; i++ ) {
			if( i > 0 ) {
				if( (i%maxwidth) == 0 ) {
					sb.append("\n");
					for( int e = 0; e < leftpadding; e++) {
						sb.append("\t");
					}
				} else {
					sb.append(" ");
				}
			}
			sb.append(table[i]);
			sb.append(",");
		}
		return sb.toString();
	}
}
