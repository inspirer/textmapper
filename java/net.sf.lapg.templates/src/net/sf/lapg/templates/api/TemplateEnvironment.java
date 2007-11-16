package net.sf.lapg.templates.api;

import java.util.HashMap;

import net.sf.lapg.templates.ast.Parser;

public abstract class TemplateEnvironment extends AbstractEnvironment {

	private HashMap<String,ITemplate> templates;

	public TemplateEnvironment() {
		this.templates = new HashMap<String,ITemplate>();
	}

	protected abstract String getTemplateContainerContents(String name);

	protected abstract String getContainerName(String templatePackage);

	private ITemplate getTemplate(ILocatedEntity referer, String name) {

		if( templates.containsKey(name) ) {
			return templates.get(name);
		}

		int lastDot = name.lastIndexOf('.');
		if( lastDot == -1 ) {
			fireError(referer, "Fully qualified template name should contain dot.");
			return null;
		}

		String templatePackage = name.substring(0, lastDot);
		String contents = getTemplateContainerContents(getContainerName(templatePackage));
		if( contents == null ) {
			fireError(referer, "Couldn't load template container for `" + name + "`");
			return null;
		}

		ITemplate[] loaded = loadTemplates(contents, templatePackage);
		if( loaded == null || loaded.length == 0 ) {
			fireError(referer, "Couldn't get templates from " + getContainerName(templatePackage));
			return null;
		}

		String id = name.substring(lastDot+1);
		ITemplate result = null;
		for( ITemplate t : loaded ) {
			if( t.getName().equals(id)) {
				result = t;
			}
			String templateId = templatePackage+"."+t.getName();
			if( templates.containsKey(templateId)) {
				fireError(t, "Template `"+templateId+"` was already defined");
			}
			templates.put(templateId, t);
		}

		if( result == null ) {
			fireError(referer, "Template `"+id+"` was not found in `" + getContainerName(templatePackage) + "`");
		}
		return result;
	}

	@Override
	public String executeTemplate(ILocatedEntity referer, String name, Object context, Object[] arguments) {
		ITemplate t = getTemplate(referer, name);
		if( t == null ) {
			return "";
		}
		try {
			return t.apply(context, this, arguments);
		} catch( EvaluationException ex ) {
			fireError(t, ex.getMessage());
			return "";
		}
	}

	public String evaluateTemplate(ILocatedEntity referer, String template, Object context) {

		// TODO replace hack with normal parser
		ITemplate[] loaded = loadTemplates("${template temp}"+template+"${end}", "temp");
		ITemplate t = ( loaded != null && loaded.length == 1 && loaded[0].getName().equals("temp") ) ? loaded[0] : null;
		if( t == null ) {
			return "";
		}
		try {
			return t.apply(context, this, null);
		} catch( EvaluationException ex ) {
			fireError(t, ex.getMessage());
			return "";
		}
	}

	private static ITemplate[] loadTemplates(String templates, String templatePackage) {
		Parser p = new Parser();
		if( !p.parse(templates, templatePackage) ) {
			return new ITemplate[0];
		}
		return p.getResult();
	}

	private IStaticMethods myStaticMethods = null;

	@Override
	public IStaticMethods getStaticMethods() {
		if( myStaticMethods == null) {
			myStaticMethods = new DefaultStaticMethods();
		}
		return myStaticMethods;
	}
}
