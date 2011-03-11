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
package org.textway.templates.objects;

/**
 * Gryaznov Evgeny, 3/11/11
 */
public class JavaIsInstanceUtil {

	public static boolean isInstance(Object o, String qualifiedName) {
		String name = o.getClass().getCanonicalName();
		if(matches(name, qualifiedName)) {
			return Boolean.TRUE;
		}
		if(qualifiedName.indexOf('.') >= 0) {
			return hasSupertype(o.getClass(), qualifiedName);
		}
		return false;
	}

	private static boolean hasSupertype(Class<? extends Object> class_, String className) {
		if(class_ == null) {
			return false;
		}
		if(matches(class_.getCanonicalName(), className)) {
			return true;
		}
		for(Class<? extends Object> i : class_.getInterfaces()) {
			if(hasSupertype(i, className)) {
				return true;
			}
		}
		return hasSupertype(class_.getSuperclass(), className);
	}

	private static boolean matches(String name, String pattern) {
		if(name == null) {
			return false;
		}
		if(pattern.indexOf('.') >= 0) {
			return name.equals(pattern);
		}
		if(name.indexOf('.') >= 0) {
			name = name.substring(name.lastIndexOf('.') + 1);
		}
		if(name.equals(pattern) || name.toLowerCase().equals(pattern)) {
			return true;
		}
		return false;
	}
}
