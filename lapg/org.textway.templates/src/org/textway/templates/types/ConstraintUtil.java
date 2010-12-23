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
package org.textway.templates.types;

import org.textway.templates.api.types.IDataType.Constraint;

import java.util.Collection;

public class ConstraintUtil {

	public static String validate(Object literal, Constraint constraint) {
		if(literal instanceof String) {
			String s = (String) literal;
			switch(constraint.getKind()) {
				case CHOICE:
					if(s.length() == 0) {
						break;
					}
					if(!(constraint.getParameters().contains(s.trim()))) {
						return "should be one of: " + join(constraint.getParameters(), ", ");
					}
					break;
				case IDENTIFIER:
					if(s.length() == 0) {
						break;
					}
					if(!isIdentifier(s)) {
						return "should be identifier";
					}
					break;
				case NOTEMPTY:
					if(s.trim().length() == 0) {
						return "should be non-empty string";
					}
					break;
				case QUALIFIED_IDENTIFIER:
					if(s.length() == 0) {
						break;
					}
					for(String part : s.split("\\.")) {
						if(!isIdentifier(part)) {
							return "should be qualified identifier";
						}
					}
					break;
				case SET:
					if(s.trim().length() > 0) {
						for(String part : s.split(",")) {
							part = part.trim();
							if(!(constraint.getParameters().contains(part))) {
								return "invalid set option `" + part + "`, should be one of: " + join(constraint.getParameters(), ", ");
							}
						}
					}
					break;
			}

		}
		return null;
	}

	public static String join(Collection<String> c, String separator) {
		StringBuilder sb = new StringBuilder();
		for(String s : c) {
			if(sb.length() != 0) {
				sb.append(separator);
			}
			sb.append(s);
		}
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
}
