package net.sf.lapg.templates.api.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.ILocatedEntity;
import net.sf.lapg.templates.api.INavigationStrategy;
import net.sf.lapg.templates.api.ITemplate;
import net.sf.lapg.templates.api.ITemplateLoader;
import net.sf.lapg.templates.ast.AstParser;

public class TemplateEnvironment extends AbstractEnvironment {

	private HashSet<String> loadedPackages;

	private HashMap<String, String> overrides;

	private HashMap<String, ITemplate> templates;

	private ITemplateLoader[] loaders;

	public TemplateEnvironment(INavigationStrategy.Factory strategy, ITemplateLoader... loaders) {
		super(strategy);
		this.templates = new HashMap<String, ITemplate>();
		this.overrides = new HashMap<String, String>();
		this.loadedPackages = new HashSet<String>();
		this.loaders = loaders;

		if (loaders == null || loaders.length < 1) {
			throw new IllegalArgumentException("no loaders provided");
		}
	}

	private String getContainerContent(String containerName) {
		for (ITemplateLoader loader : loaders) {
			String result = loader.load(containerName);
			if (result != null) {
				return result;
			}
		}
		return null;
	}

	public void loadPackage(ILocatedEntity referer, String packageName) {
		if (loadedPackages.contains(packageName)) {
			return;
		}

		String contents = getContainerContent(packageName);
		if (contents == null) {
			fireError(referer, "Couldn't load template package `" + packageName + "`");
			return;
		}

		ITemplate[] loaded = loadTemplates(contents, packageName, packageName + ITemplateLoader.CONTAINER_EXT);
		if (loaded == null || loaded.length == 0) {
			fireError(referer, "Couldn't get templates from package " + packageName);
			return;
		}

		for (ITemplate t : loaded) {
			String templateId = packageName + "." + t.getName();
			if (templates.containsKey(templateId)) {
				fireError(t, "Template `" + templateId + "` was already defined");
			}
			String overridden = t.getOverridden();
			if (overridden != null) {
				if (overridden.indexOf('.') == -1) {
					overridden = packageName + "." + overridden;
				}
				if (overrides.containsKey(overridden)) {
					fireError(t, "Template `" + overridden + "` was already redeclared with `" + overrides.get(overridden) + "`, cannot override in `" + templateId + "`");
				} else {
					overrides.put(overridden, templateId);
				}
			}
			templates.put(templateId, t);
		}
		loadedPackages.add(packageName);
	}

	private ITemplate getTemplate(ILocatedEntity referer, String qualifiedName, boolean searchOverrides) {
		int lastDot = qualifiedName.lastIndexOf('.');
		if (lastDot == -1) {
			fireError(referer, "Fully qualified template name should contain dot.");
			return null;
		}

		String templatePackage = qualifiedName.substring(0, lastDot);
		loadPackage(referer, templatePackage);

		String resolvedName = qualifiedName;

		if (searchOverrides) {
			while (overrides.containsKey(resolvedName)) {
				resolvedName = overrides.get(resolvedName);
			}
		}

		if (!templates.containsKey(resolvedName)) {
			fireError(referer, "Template `" + resolvedName + "` was not found in package `" + templatePackage + "`");
		}
		return templates.get(resolvedName);
	}

	@Override
	public String executeTemplate(ILocatedEntity referer, String name, EvaluationContext context, Object[] arguments) {
		boolean searchOverrides = true;
		if (name.equals("base")) {
			ITemplate current = context.getCurrentTemplate();
			if (current != null) {
				if (current.getOverridden() == null) {
					fireError(referer, "Cannot find overriden template for `" + current.getName() + "`");
				} else {
					name = current.getOverridden();
					searchOverrides = false;
				}
			}
		}
		ITemplate t = getTemplate(referer, name, searchOverrides);
		if (t == null) {
			return "";
		}
		try {
			return t.apply(new EvaluationContext(context != null ? context.getThisObject() : null, context, t), this, arguments);
		} catch (EvaluationException ex) {
			fireError(t, ex.getMessage());
			return "";
		}
	}

	public String evaluateTemplate(ILocatedEntity referer, String template, String templateId, EvaluationContext context) {

		// TODO replace hack with normal parser
		ITemplate[] loaded = loadTemplates("${template temp}" + template + "${end}", "temp", templateId != null ? templateId : referer.getLocation());
		ITemplate t = (loaded != null && loaded.length == 1 && loaded[0].getName().equals("temp")) ? loaded[0] : null;
		if (t == null) {
			return "";
		}
		try {
			return t.apply(context, this, null);
		} catch (EvaluationException ex) {
			fireError(t, ex.getMessage());
			return "";
		}
	}

	private static ITemplate[] loadTemplates(String templates, String templatePackage, String inputName) {
		AstParser p = new AstParser();
		if (!p.parse(templates, templatePackage, inputName)) {
			return new ITemplate[0];
		}
		return p.getResult();
	}

	public void createFile(String name, String contents) {
		// do nothing by default
	}

	public Iterator<?> getCollectionIterator(Object o) {
		if (o instanceof Collection) {
			return ((Collection<?>) o).iterator();
		}
		if (o instanceof Object[]) {
			return new ArrayIterator((Object[]) o);
		}
		return null;
	}

	private static class ArrayIterator implements Iterator<Object> {

		Object[] elements;

		int index;

		int lastElement;

		public ArrayIterator(Object[] elements) {
			this(elements, 0, elements.length - 1);
		}

		public ArrayIterator(Object[] elements, int firstElement, int lastElement) {
			this.elements = elements;
			index = firstElement;
			this.lastElement = lastElement;
		}

		public boolean hasNext() {
			return elements != null && index <= lastElement;
		}

		public Object next() throws NoSuchElementException {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			return elements[index++];
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
