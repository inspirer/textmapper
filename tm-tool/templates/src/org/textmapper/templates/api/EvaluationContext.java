/**
 * Copyright 2002-2016 Evgeny Gryaznov
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
package org.textmapper.templates.api;

import org.textmapper.templates.bundle.IBundleEntity;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Template invocation context. Contains variables and `this' reference.
 */
public class EvaluationContext {

	public static Object NULL_VALUE = new Object() {
		@Override
		public String toString() {
			return "null";
		}
	};

	private Map<String, Object> vars;
	private final Object thisObject;
	private final SourceElement caller;
	private final EvaluationContext parent;
	private final IBundleEntity current;

	public EvaluationContext(Object thisObject) {
		this(thisObject, null, null);
	}

	public EvaluationContext(Object thisObject, SourceElement caller, EvaluationContext parent) {
		this(thisObject, caller, parent, parent != null ? parent.getCurrent() : null);
	}

	public EvaluationContext(Object thisObject, SourceElement caller, EvaluationContext parent, IBundleEntity current) {
		this.thisObject = thisObject;
		this.caller = caller;
		this.parent = parent;
		this.current = current;
	}

	public Object getVariable(String id) {
		if (parent != null && (vars == null || !vars.containsKey(id))) {
			return parent.getVariable(id);
		}
		return vars != null ? vars.get(id) : null;
	}

	public void setVariable(String id, Object value) {
		if (vars == null) {
			vars = new HashMap<>();
		}
		vars.put(id, value);
	}

	public Object getThisObject() {
		return thisObject;
	}

	public SourceElement getCaller() {
		return caller;
	}

	public IBundleEntity getCurrent() {
		return current;
	}

	public void printStackTrace(SourceElement element, PrintStream s) {
		if (element != null) {
			s.println("at " + getShortResourceName(element.getResourceName()) + "," + element.getLine());
		}
		s.println("\t\tthis = " + getPresentableValue(getThisObject()));
		if (vars != null) {
			String[] list = vars.keySet().toArray(new String[vars.size()]);
			Arrays.sort(list);
			for (String v : list) {
				s.println("\t\t" + v + " = " + getPresentableValue(vars.get(v)));
			}
		}
		if (parent != null) {
			parent.printStackTrace(caller, s);
		} else {
			s.println();
		}
	}

	static String getPresentableValue(Object o) {
		if (o == null) {
			return null;
		}
		if (o instanceof Integer || o instanceof Boolean) {
			return o.toString();
		}
		if (o instanceof String) {
			return "\"" + escape((String) o) + "\"";
		}
		if (o instanceof Collection) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			for (Object i : (Collection) o) {
				if (sb.length() > 1) {
					sb.append(",");
				}
				String val = getPresentableValue(i);
				if (sb.length() + val.length() < 80) {
					sb.append(val);
				} else {
					sb.append("...");
					break;
				}
			}
			sb.append("]");
		}

		return "[class]" + o.getClass().getSimpleName();
	}

	static String getShortResourceName(String longName) {
		int slash = Math.max(longName.lastIndexOf('/'), longName.lastIndexOf('\\'));
		if (slash >= 0 && slash < longName.length() - 1) {
			return longName.substring(slash + 1);
		}
		return longName;
	}

	static String escape(String s) {
		StringBuilder sb = new StringBuilder();
		for (char c : s.toCharArray()) {
			switch (c) {
				case '"':
				case '\'':
				case '\\':
					sb.append('\\');
					sb.append(c);
					continue;
				case '\f':
					sb.append("\\f");
					continue;
				case '\n':
					sb.append("\\n");
					continue;
				case '\r':
					sb.append("\\r");
					continue;
				case '\t':
					sb.append("\\t");
					continue;
			}
			if (c >= 0x20 && c < 0x80) {
				sb.append(c);
				continue;
			}
			appendEscaped(sb, c);
		}
		return sb.toString();
	}

	static void appendEscaped(StringBuilder sb, char c) {
		String sym = Integer.toString(c, 16);
		boolean isShort = sym.length() <= 2;
		sb.append(isShort ? "\\x" : "\\u");
		int len = isShort ? 2 : 4;
		if (sym.length() < len) {
			sb.append("0000".substring(sym.length() + (4 - len)));
		}
		sb.append(sym);
	}

}
