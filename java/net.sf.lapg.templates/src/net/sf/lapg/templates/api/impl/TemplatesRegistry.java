package net.sf.lapg.templates.api.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.lapg.templates.api.ILocatedEntity;
import net.sf.lapg.templates.api.IBundleEntity;
import net.sf.lapg.templates.api.IProblemCollector;
import net.sf.lapg.templates.api.IBundleLoader;
import net.sf.lapg.templates.api.TemplatesBundle;

public class TemplatesRegistry {

	private final IProblemCollector collector;
	private final Set<String> loadedBundles;
	private final Map<String, IBundleEntity> entities;
	private final IBundleLoader[] loaders;

	public TemplatesRegistry(IProblemCollector collector, IBundleLoader... loaders) {
		this.collector = collector;
		this.entities = new HashMap<String, IBundleEntity>();
		this.loadedBundles = new HashSet<String>();
		this.loaders = loaders;

		if (loaders == null || loaders.length < 1) {
			throw new IllegalArgumentException("no loaders provided");
		}
	}

	private TemplatesBundle[] getBundleContents(String bundleName) {
		List<TemplatesBundle> result = new LinkedList<TemplatesBundle>();
		for (IBundleLoader loader : loaders) {
			TemplatesBundle source = loader.load(bundleName, collector);
			if (source != null) {
				result.add(source);
			}
		}
		return result.size() > 0 ? result.toArray(new TemplatesBundle[result.size()]) : null;
	}

	private void loadBundle(ILocatedEntity referer, String bundleName) {
		if (loadedBundles.contains(bundleName)) {
			return;
		}

		TemplatesBundle[] contents = getBundleContents(bundleName);
		if (contents == null) {
			collector.fireError(referer, "Couldn't load package `" + bundleName + "`");
			return;
		}

		Map<String, IBundleEntity> nameToEntity = new HashMap<String, IBundleEntity>();
		Set<String> seenNames = new HashSet<String>();
		for (int i = contents.length - 1; i >= 0; i--) {
			IBundleEntity[] loaded = contents[i].getEntities();
			if (loaded == null || loaded.length == 0) {
				collector.fireError(referer, "Couldn't get templates from " + contents[i].getName());
				return;
			}

			seenNames.clear();
			for (IBundleEntity t : loaded) {
				String name = t.getName();
				IBundleEntity base = nameToEntity.get(name);
				if (seenNames.contains(name)) {
					String baseKind = base != null ? base.getKindAsString() : "Element";
					collector.fireError(t, baseKind + " `" + bundleName + "." + name + "` was already defined");
				} else {
					if (base != null) {
						if (base.getKind() != t.getKind()) {
							collector.fireError(t, t.getKindAsString() + " `" + t.toString()
									+ "` is not compatible with base " + base.getKindAsString() + " `"
									+ base.toString() + "`");
						} else if (!base.getSignature().equals(t.getSignature())) {
							collector.fireError(t, t.getKindAsString() + " `" + t.toString()
									+ "` is not compatible with base `" + base.toString() + "`");
						} else {
							t.setBase(base);
						}
					}
					nameToEntity.put(name, t);
				}
			}
		}

		loadedBundles.add(bundleName);
		for (Map.Entry<String, IBundleEntity> entry : nameToEntity.entrySet()) {
			entities.put(bundleName + "." + entry.getKey(), entry.getValue());
		}
	}

	public IBundleEntity getEntity(ILocatedEntity referer, String qualifiedName) {
		int lastDot = qualifiedName.lastIndexOf('.');
		if (lastDot == -1) {
			collector.fireError(referer, "Fully qualified name should contain dot.");
			return null;
		}

		String templatePackage = qualifiedName.substring(0, lastDot);
		loadBundle(referer, templatePackage);

		String resolvedName = qualifiedName;

		if (!entities.containsKey(resolvedName)) {
			collector.fireError(referer, "Element `" + resolvedName + "` was not found in package `" + templatePackage
					+ "`");
		}
		return entities.get(resolvedName);
	}
}
