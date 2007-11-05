package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.IEvaluationEnvironment;
import net.sf.lapg.templates.api.ILocatedEntity;

public abstract class Node implements ILocatedEntity {

	private int line;

	protected Node(int line) {
		this.line = line;
	}

	protected abstract void emit( StringBuffer sb, Object context, IEvaluationEnvironment env);

	public String getLocation() {
		return "line:" + line;
	}
}
