package net.sf.lapg.templates.api;

import java.util.HashMap;

/**
 * Context for one template invocation. Contains variables and this object.
 */
public class EvaluationContext {

	private HashMap<String, Object> vars;
	private final Object thisObject;
	private final EvaluationContext parent;
	private final ITemplate currentTemplate;

	public EvaluationContext(Object thisObject) {
		this(thisObject, null, null);
	}

	public EvaluationContext(Object thisObject, EvaluationContext parent) {
		this(thisObject, parent, parent != null ? parent.getCurrentTemplate() : null);
	}

	public EvaluationContext(Object thisObject, EvaluationContext parent, ITemplate currentTemplate) {
		this.thisObject = thisObject;
		this.parent = parent;
		this.currentTemplate = currentTemplate;
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

	public ITemplate getCurrentTemplate() {
		return currentTemplate;
	}
}
