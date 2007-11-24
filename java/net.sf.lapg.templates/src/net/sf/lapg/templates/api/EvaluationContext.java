package net.sf.lapg.templates.api;

import java.util.HashMap;


public class EvaluationContext {

	private HashMap<String,Object> vars;
	private final Object thisObject;
	private final EvaluationContext parent;

	public EvaluationContext(Object thisObject) {
		this(thisObject, null);
	}

	public EvaluationContext(Object thisObject, EvaluationContext parent) {
		this.thisObject = thisObject;
		this.parent = parent;
	}

	public Object getVariable(String id) {

		if( parent != null && (vars == null || !vars.containsKey(id)) ) {
			return parent.getVariable(id);
		}

		return vars != null ? vars.get(id) : null;
	}

	public void setVariable(String id, Object value) {
		if( vars == null ) {
			vars = new HashMap<String, Object>();
		}
		vars.put(id, value);
	}

	public Object getThisObject() {
		return thisObject;
	}
}
