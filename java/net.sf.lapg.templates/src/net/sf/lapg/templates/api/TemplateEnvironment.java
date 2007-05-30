package net.sf.lapg.templates.api;

import java.util.HashMap;

import net.sf.lapg.templates.parser.Parser;

public abstract class TemplateEnvironment extends AbstractEnvironment {
	
	private HashMap<String,ITemplate> templates;

	public TemplateEnvironment() {
		this.templates = new HashMap<String,ITemplate>();
	}

	protected abstract String getTemplateContainerContents(String name);
	
	protected abstract String getContainerName(String templatePackage);
	
	private ITemplate getTemplate(String name) {
		if( templates.containsKey(name) )
			return templates.get(name);

		int lastDot = name.lastIndexOf('.');
		if( lastDot == -1 ) {
			fireError("Fully qualified template name should contain dot.");
			return null;
		}
		
		String templatePackage = name.substring(0, lastDot);
		String contents = getTemplateContainerContents(getContainerName(templatePackage));
		if( contents == null ) {
			fireError("Couldn't load template container for `" + name + "`");
			return null;
		}

		ITemplate[] loaded = loadTemplates(contents, templatePackage); 
		if( loaded == null || loaded.length == 0 ) {
			fireError("Couldn't get templates from " + getContainerName(templatePackage));
			return null;
		}

		String id = name.substring(lastDot+1);
		ITemplate result = null;
		for( ITemplate t : loaded ) {
			if( t.getName().equals(id))
				result = t;
			templates.put(templatePackage+"."+t.getName(), t);
		}
		
		if( result == null ) {
			fireError("Template `"+id+"` was not found in `" + getContainerName(templatePackage) + "`");
		}
		return result;
	}

	public String executeTemplate(String name, Object context, Object[] arguments) {
		ITemplate t = getTemplate(name);
		if( t == null )
			return "";
		try {
			return t.apply(context, this, arguments);
		} catch( EvaluationException ex ) {
			fireError(ex.getMessage());
			return "";
		}
	}

	private static ITemplate[] loadTemplates(String templates, String templatePackage) {
		Parser p = new Parser();
		if( !p.parse(templates, templatePackage) )
			return new ITemplate[0];
		return p.getResult();
	}
}
