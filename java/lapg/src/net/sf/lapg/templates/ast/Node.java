package net.sf.lapg.templates.ast;

import net.sf.lapg.templates.ExecutionEnvironment;

public abstract class Node {
	protected abstract void emit( StringBuffer sb, Object context, ExecutionEnvironment env);
}
