package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.api.IEvaluationEnvironment;

public abstract class Node {
	protected abstract void emit( StringBuffer sb, Object context, IEvaluationEnvironment env);
}
