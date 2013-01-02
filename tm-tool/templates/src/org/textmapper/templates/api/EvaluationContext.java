/**
 * Copyright 2002-2013 Evgeny Gryaznov
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

import java.util.HashMap;
import java.util.Map;

/**
 * Context for one template invocation. Contains variables and `this' reference.
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
	private final EvaluationContext parent;
	private final IBundleEntity current;

	public EvaluationContext(Object thisObject) {
		this(thisObject, null, null);
	}

	public EvaluationContext(Object thisObject, EvaluationContext parent) {
		this(thisObject, parent, parent != null ? parent.getCurrent() : null);
	}

	public EvaluationContext(Object thisObject, EvaluationContext parent, IBundleEntity current) {
		this.thisObject = thisObject;
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
			vars = new HashMap<String, Object>();
		}
		vars.put(id, value);
	}

	public Object getThisObject() {
		return thisObject;
	}

	public IBundleEntity getCurrent() {
		return current;
	}
}
