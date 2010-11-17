/**
 * Copyright 2002-2010 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.textway.templates.types;

import java.util.*;

import org.textway.templates.bundle.ILocatedEntity;
import org.textway.templates.bundle.TemplatesRegistry;
import org.textway.templates.api.types.IClass;
import org.textway.templates.api.types.ITypesRegistry;

public class TypesRegistry implements ITypesRegistry {

	private TemplatesRegistry myResourceRegistry;
	private Set<String> myLoadedPackages = new HashSet<String>();
	private Map<String, TiClass> myClasses = new HashMap<String, TiClass>();

	public TypesRegistry(TemplatesRegistry myResourceRegistry) {
		this.myResourceRegistry = myResourceRegistry;
	}

	public IClass loadClass(String qualifiedName, ILocatedEntity referer) {
		int lastDot = qualifiedName.lastIndexOf('.');
		if (lastDot == -1) {
			myResourceRegistry.getCollector().fireError(referer, "Fully qualified type name should contain dot.");
			return null;
		}

		String package_ = qualifiedName.substring(0, lastDot);
		if(!myLoadedPackages.contains(package_)) {
			loadPackage(package_, referer);
		}
		return myClasses.get(qualifiedName);
	}

	private void loadPackage(String name, ILocatedEntity referer) {
		LinkedHashSet<String> queue = new LinkedHashSet<String>();
		queue.add(name);
		List<TiResolver> loaders = new ArrayList<TiResolver>();

		// 1-st stage: load types
		while(!queue.isEmpty()) {
			String current = queue.iterator().next();
			queue.remove(current);
			assert !myLoadedPackages.contains(current);
			myLoadedPackages.add(current);
			
			String[] contentLayers = myResourceRegistry.loadResource(current, "types");
			if(contentLayers == null || contentLayers.length < 1) {
				myResourceRegistry.getCollector().fireError(referer, "Couldn't load types package `" + current + "`");
				continue;
			}

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

		// 2-nd stage: resolve references
		for(TiResolver resolver : loaders) {
			resolver.resolve();
		}
	}
}
