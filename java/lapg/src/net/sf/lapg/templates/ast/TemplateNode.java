package net.sf.lapg.templates.ast;

import java.io.PrintStream;

import net.sf.lapg.templates.ITemplate;

public class TemplateNode extends CompoundNode implements ITemplate {
	private String name;

	public TemplateNode(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String apply(Object context, PrintStream errors) {
		// TODO Auto-generated method stub
		StringBuffer sb = new StringBuffer();
		emit(sb, context);
		return sb.toString();
	}
}
