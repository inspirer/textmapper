package net.sf.lapg.templates.api.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.lapg.templates.api.ILocatedEntity;
import net.sf.lapg.templates.api.IProblemCollector;
import net.sf.lapg.templates.api.ITemplate;
import net.sf.lapg.templates.api.ITemplateLoader;
import net.sf.lapg.templates.api.TemplatesPackage;

public class TemplatesRegistry {

	private final IProblemCollector collector;

	private final HashSet<String> loadedPackages;

	private final HashMap<String, ITemplate> templates;

	private final ITemplateLoader[] loaders;

	public TemplatesRegistry(IProblemCollector collector, ITemplateLoader... loaders) {
		this.collector = collector;
		this.templates = new HashMap<String, ITemplate>();
		this.loadedPackages = new HashSet<String>();
		this.loaders = loaders;

		if (loaders == null || loaders.length < 1) {
			throw new IllegalArgumentException("no loaders provided");
		}
	}

	private TemplatesPackage[] getContainerContent(String containerName) {
		List<TemplatesPackage> result = new LinkedList<TemplatesPackage>();
		for (ITemplateLoader loader : loaders) {
			TemplatesPackage source = loader.load(containerName, collector);
			if (source != null) {
				result.add(source);
			}
		}
		return result.size() > 0 ? result.toArray(new TemplatesPackage[result.size()]) : null;
	}

	private void loadPackage(ILocatedEntity referer, String packageName) {
		if (loadedPackages.contains(packageName)) {
			return;
		}

		TemplatesPackage[] contents = getContainerContent(packageName);
		if (contents == null) {
			collector.fireError(referer, "Couldn't load template package `" + packageName + "`");
			return;
		}

		Map<String, ITemplate> nameToTemplate = new HashMap<String, ITemplate>();
		Set<String> seenNames = new HashSet<String>();
		for(int i = contents.length-1; i >= 0; i--) {
			ITemplate[] loaded = contents[i].getTemplates();
			if (loaded == null || loaded.length == 0) {
				collector.fireError(referer, "Couldn't get templates from " + contents[i].getName());
				return;
			}

			seenNames.clear();
			for (ITemplate t : loaded) {
				String name = t.getName();
				if(seenNames.contains(name)) {
					collector.fireError(t, "Template `" + packageName + "." + name + "` was already defined");
				} else {
					ITemplate base = nameToTemplate.get(name);
					if(base != null) {
						boolean isCompatible = base.getSignature().equals(t.getSignature());
						if(isCompatible) {
							t.setBase(base);
						} else {
							collector.fireError(t, "Template `" + t.toString() + "` is not compatible with base template `" + base.toString() + "`");
						}
					}
					nameToTemplate.put(name, t);
				}
			}
		}

		loadedPackages.add(packageName);
		for(Map.Entry<String, ITemplate> entry : nameToTemplate.entrySet()) {
			templates.put(packageName + "." + entry.getKey(), entry.getValue());
		}
	}

	public ITemplate getTemplate(ILocatedEntity referer, String qualifiedName) {
		int lastDot = qualifiedName.lastIndexOf('.');
		if (lastDot == -1) {
			collector.fireError(referer, "Fully qualified template name should contain dot.");
			return null;
		}

		String templatePackage = qualifiedName.substring(0, lastDot);
		loadPackage(referer, templatePackage);

		String resolvedName = qualifiedName;

		if (!templates.containsKey(resolvedName)) {
			collector.fireError(referer, "Template `" + resolvedName + "` was not found in package `" + templatePackage + "`");
		}
		return templates.get(resolvedName);
	}
}
