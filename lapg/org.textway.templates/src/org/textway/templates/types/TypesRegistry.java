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

import org.textway.templates.api.IProblemCollector;
import org.textway.templates.api.types.IClass;
import org.textway.templates.api.types.ITypesRegistry;
import org.textway.templates.bundle.ILocatedEntity;
import org.textway.templates.storage.IResourceLoader;
import org.textway.templates.storage.Resource;
import org.textway.templates.storage.ResourceRegistry;

import java.util.*;

public class TypesRegistry implements ITypesRegistry {

	private final ResourceRegistry myResourceRegistry;
	private final IProblemCollector myCollector;
	private final Set<String> myLoadedPackages = new HashSet<String>();
	private final Map<String, TiClass> myClasses = new HashMap<String, TiClass>();

	public TypesRegistry(ResourceRegistry resourceRegistry, IProblemCollector collector) {
		myResourceRegistry = resourceRegistry;
		myCollector = collector;
	}

	public IClass loadClass(String qualifiedName, ILocatedEntity referer) {
		int lastDot = qualifiedName.lastIndexOf('.');
		if (lastDot == -1) {
			myCollector.fireError(referer, "Fully qualified type name should contain dot.");
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
		List<TypesResolver> loaders = new ArrayList<TypesResolver>();

		// 1-st stage: load types
		while(!queue.isEmpty()) {
			String current = queue.iterator().next();
			queue.remove(current);
			assert !myLoadedPackages.contains(current);
			myLoadedPackages.add(current);

			Resource[] contentLayers = myResourceRegistry.loadResources(current, IResourceLoader.KIND_TYPES);
			if(contentLayers == null || contentLayers.length < 1) {
				myCollector.fireError(referer, "Couldn't load types package `" + current + "`");
				continue;
			}

			for(Resource content : contentLayers) {
				TypesResolver resolver = new TypesResolver(current, content, myClasses, myCollector);
				resolver.build();
				for(String package_ : resolver.getRequired()) {
					if(!myLoadedPackages.contains(package_)) {
						queue.add(package_);
					}
				}
				loaders.add(resolver);
			}
		}

		// 2-nd stage: resolve references
		for(TypesResolver resolver : loaders) {
			resolver.resolve();
		}

		// 3-d stage: resolve expressions
		for(TypesResolver resolver : loaders) {
			resolver.resolveExpressions();
		}
	}
}
