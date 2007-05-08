package net.sf.lapg.templates.ast;

public abstract class Node {
	protected abstract void emit( StringBuffer sb, Object context);
}
