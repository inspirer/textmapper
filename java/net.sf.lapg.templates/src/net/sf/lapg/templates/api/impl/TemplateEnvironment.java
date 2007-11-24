package net.sf.lapg.templates.api.impl;

import java.util.HashMap;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.ILocatedEntity;
import net.sf.lapg.templates.api.INavigationStrategy;
import net.sf.lapg.templates.api.ITemplate;
import net.sf.lapg.templates.api.ITemplateLoader;
import net.sf.lapg.templates.ast.Parser;

public class TemplateEnvironment extends AbstractEnvironment {

	private HashMap<String,ITemplate> templates;
	private ITemplateLoader[] loaders;

	public TemplateEnvironment(INavigationStrategy.Factory strategy, ITemplateLoader... loaders) {
		super(strategy);
		this.templates = new HashMap<String,ITemplate>();
		this.loaders = loaders;

		if( loaders == null || loaders.length < 1) {
			throw new IllegalArgumentException("no loaders provided");
		}
	}

	private String getContainerContent(String containerName) {
		for(ITemplateLoader loader : loaders) {
			String result = loader.load(containerName);
			if( result != null ) {
				return result;
			}
		}
		return null;
	}

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
		String contents = getContainerContent(templatePackage);
		if( contents == null ) {
			fireError(referer, "Couldn't load template container for `" + name + "`");
			return null;
		}

		ITemplate[] loaded = loadTemplates(contents, templatePackage);
		if( loaded == null || loaded.length == 0 ) {
			fireError(referer, "Couldn't get templates from package " + templatePackage);
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
			fireError(referer, "Template `"+id+"` was not found in package `" + templatePackage + "`");
		}
		return result;
	}

	@Override
	public String executeTemplate(ILocatedEntity referer, String name, EvaluationContext context, Object[] arguments) {
		if( context == null ) {
			context = new EvaluationContext(null);
		}
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

	public String evaluateTemplate(ILocatedEntity referer, String template, EvaluationContext context) {

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
}
