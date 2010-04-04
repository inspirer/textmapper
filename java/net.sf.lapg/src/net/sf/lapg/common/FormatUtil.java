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
package net.sf.lapg.common;

public class FormatUtil {

	public static String asHex(int i, int width) {
		String s = Integer.toHexString(i);
		if( s.length() >= width ) {
			return s;
		}
		StringBuffer sb = new StringBuffer();
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
		StringBuffer sb = new StringBuffer();
		for( int chars = width - s.length(); chars > 0; chars-- ) {
			sb.append(padding);
		}
		sb.append(s);
		return sb.toString();
	}
}
