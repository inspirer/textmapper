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
					String baseKind = base != null ? kindToString(base.getKind()) : "Element";
					collector.fireError(t, baseKind + " `" + bundleName + "." + name + "` was already defined");
				} else {
					if (base != null ) {
						if (base.getKind() != t.getKind() || !base.getSignature().equals(t.getSignature())) {
							collector.fireError(t, kindToString(t.getKind()) + " `" + t.toString()
									+ "` is not compatible with base " + kindToString(base.getKind()).toLowerCase()
									+ " `" + base.toString() + "`");
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

	public IBundleEntity loadEntity(String qualifiedName, int kind, ILocatedEntity referer) {
		int lastDot = qualifiedName.lastIndexOf('.');
		if (lastDot == -1) {
			collector.fireError(referer, "Fully qualified name should contain dot.");
			return null;
		}

		String templatePackage = qualifiedName.substring(0, lastDot);
		loadBundle(referer, templatePackage);

		String resolvedName = qualifiedName;

		IBundleEntity t = entities.get(resolvedName);
		if (t == null || kind != IBundleEntity.KIND_ANY && t.getKind() != kind) {
			collector.fireError(referer, kindToString(kind) + " `" + resolvedName + "` was not found in package `"
					+ templatePackage + "`");
			t = null;
		}
		return t;
	}

	private static String kindToString(int kind) {
		if (kind == IBundleEntity.KIND_QUERY) {
			return "Query";
		} else if (kind == IBundleEntity.KIND_TEMPLATE) {
			return "Template";
		} else {
			return "Element";
		}
	}
}
