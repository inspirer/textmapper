package net.sf.lapg.gen.template;

public class TemplateContext {
	
	TemplateContext parent;
	StringBuffer result = new StringBuffer();
	
	public TemplateContext( TemplateContext parent ) {
		this.parent = parent;
	}

	public void start() {
	}
	
	public void end() {
	}
	
	public void accept(String s) {
		result.append(s);
	}
	
	public void acceptVar(String var) {
		result.append("valueof("+var+")");
	}

	public TemplateContext getParent() {
		return parent;
	}

	public String toString() {
		return result.toString();
	}
}
