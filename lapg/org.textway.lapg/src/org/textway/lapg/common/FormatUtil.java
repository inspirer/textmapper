/**
 * Copyright 2002-2010 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.textway.lapg.common;

import java.util.HashMap;
import java.util.Map;

public class FormatUtil {

	public static String asHex(int i, int width) {
		String s = Integer.toHexString(i);
		if( s.length() >= width ) {
			return s;
		}
		StringBuilder sb = new StringBuilder();
		for( int chars = width - s.length(); chars > 0; chars-- ) {
			sb.append('0');
		}
		sb.append(s);
		return sb.toString();
	}

	public static String asDecimal(int i, int width, char padding) {
		String s = Integer.toString(i);
		if( s.length() >= width ) {
			return s;
		}
		StringBuilder sb = new StringBuilder();
		for( int chars = width - s.length(); chars > 0; chars-- ) {
			sb.append(padding);
		}
		sb.append(s);
		return sb.toString();
	}

    public static boolean isIdentifier(String s) {
        if(s == null || s.length() == 0) {
            return false;
        }
        char[] c = s.toCharArray();
        for(int i = 0; i < c.length; i++) {
            if(!(
                    c[i] >= 'a' && c[i] <= 'z' ||
                    c[i] >= 'A' && c[i] <= 'Z' ||
                    c[i] == '_' ||
                    i > 0 && c[i] >= '0' && c[i] <= '9')) {
                return false;
            }
        }
        return true;
    }

    public static String toIdentifier(String original, int number) {
        String s = original;

        // handle symbol names
        if (s.startsWith("\'") && s.endsWith("\'") && s.length() > 2) {
            s = s.substring(1, s.length() - 1);
            if(s.length() == 1 && isIdentifier(s)) {
                s = "char_" + s;
            }
        } else if(s.equals("{}")) {
            s = "_sym" + number;
        }

        if(isIdentifier(s)) {
            return s;
        }

        // convert
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            int c = s.charAt(i);
            if (c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_' || c >= '0' && c <= '9' && res.length() > 0) {
                res.append((char)c);
            } else {
                String name = charName.get((char)c);
                if(name == null) {
                    name = "N" + FormatUtil.asHex(c, 2);
                }
                res.append(name);
            }
        }

        return res.toString();
    }

    private static Map<Character,String> charName = buildCharactersMap();

    private static Map<Character, String> buildCharactersMap() {
        Map<Character, String> map = new HashMap<Character, String>();
        map.put('\t',"TAB");
        map.put('\n',"LF");
        map.put('\r',"CR");

        // 0x20
        map.put(' ',"SPACE");
        map.put('!',"EXCLAMATION");
        map.put('"',"QUOTE");
        map.put('#',"SHARP");
        map.put('$',"DOLLAR");
        map.put('%',"PERCENT");
        map.put('&',"AMPERSAND");
        map.put('\'',"APOSTROPHE");
        map.put('(',"LPAREN");
        map.put(')',"RPAREN");
        map.put('*',"MULT");
        map.put('+',"PLUS");
        map.put(',',"COMMA");
        map.put('-',"MINUS");
        map.put('.',"DOT");
        map.put('/',"SLASH");

        // 0x3A
        map.put(':',"COLON");
        map.put(';',"SEMICOLON");
        map.put('<',"LESS");
        map.put('=',"EQUAL");
        map.put('>',"GREATER");
        map.put('?',"QUESTIONMARK");
        map.put('@',"ATSIGN");

        // 0x5B
        map.put('[',"LSQUARE");
        map.put('\\',"BACKSLASH");
        map.put(']',"RSQUARE");
        map.put('^',"XOR");

        // 0x60
        map.put('`',"GRAVEACCENT");

        // 0x7B 
        map.put('{',"LCURLY");
        map.put('|',"OR");
        map.put('}',"RCURLY");
        map.put('~',"TILDE");

        return map;
    }
}
