package org.textway.templates.types;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.textway.templates.api.ILocatedEntity;
import org.textway.templates.api.impl.TemplatesRegistry;
import org.textway.templates.api.types.IClass;
import org.textway.templates.api.types.ITypesRegistry;

public class TypesRegistry implements ITypesRegistry {

	private TemplatesRegistry myResourceRegistry;
	private Set<String> myLoadedPackages;
	private Map<String, TiClass> myClasses;

	public TypesRegistry(TemplatesRegistry myResourceRegistry) {
		this.myResourceRegistry = myResourceRegistry;
	}

	@Override
	public IClass loadClass(String qualifiedName, ILocatedEntity referer) {
		int lastDot = qualifiedName.lastIndexOf('.');
		if (lastDot == -1) {
			myResourceRegistry.getCollector().fireError(referer, "Fully qualified name should contain dot.");
			return null;
		}

		String package_ = qualifiedName.substring(0, lastDot);
		if(!myLoadedPackages.contains(package_)) {
			loadPackage(package_);
		}		
		return myClasses.get(qualifiedName);
	}

	private void loadPackage(String name) {
		LinkedHashSet<String> queue = new LinkedHashSet<String>();
		queue.add(name);
		List<TiResolver> loaders = new ArrayList<TiResolver>();
		
		while(!queue.isEmpty()) {
			String current = queue.iterator().next();
			queue.remove(current);
			assert !myLoadedPackages.contains(current);
			myLoadedPackages.add(current);
			
			String[] contentLayers = myResourceRegistry.loadResource(current, "types");
			for(String content : contentLayers) {
				TiResolver resolver = new TiResolver(current, content, myClasses, myResourceRegistry.getCollector());
				resolver.build();
				for(String package_ : resolver.getRequired()) {
					if(!myLoadedPackages.contains(package_)) {
						queue.add(package_);
					}
				}
			}
		}

		for(TiResolver resolver : loaders) {
			resolver.resolve();
		}
	}
}
