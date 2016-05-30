/**
 * Copyright 2002-2016 Evgeny Gryaznov
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
package org.textmapper.templates.types;

import org.textmapper.templates.api.SourceElement;
import org.textmapper.templates.api.TemplatesStatus;
import org.textmapper.templates.api.types.IClass;
import org.textmapper.templates.api.types.ITypesRegistry;
import org.textmapper.templates.storage.IResourceLoader;
import org.textmapper.templates.storage.Resource;
import org.textmapper.templates.storage.ResourceRegistry;

import java.util.*;

public class TypesRegistry implements ITypesRegistry {

	private final ResourceRegistry myResourceRegistry;
	private final TemplatesStatus myStatus;
	private final Set<String> myLoadedPackages = new HashSet<>();
	private final Map<String, TiClass> myClasses = new HashMap<>();

	public TypesRegistry(ResourceRegistry resourceRegistry, TemplatesStatus status) {
		myResourceRegistry = resourceRegistry;
		myStatus = status;
	}

	@Override
	public IClass getClass(String qualifiedName, SourceElement referer) {
		int lastDot = qualifiedName.lastIndexOf('.');
		if (lastDot == -1) {
			myStatus.report(TemplatesStatus.KIND_ERROR,
					"Fully qualified type name should contain dot.", referer);
			return null;
		}

		String package_ = qualifiedName.substring(0, lastDot);
		if (!myLoadedPackages.contains(package_)) {
			loadPackage(package_, referer);
		}
		return myClasses.get(qualifiedName);
	}

	private void loadPackage(String name, SourceElement referer) {
		LinkedHashSet<String> queue = new LinkedHashSet<>();
		queue.add(name);
		List<TypesResolver> loaders = new ArrayList<>();

		// 1-st stage: load types
		while (!queue.isEmpty()) {
			String current = queue.iterator().next();
			queue.remove(current);
			assert !myLoadedPackages.contains(current);
			myLoadedPackages.add(current);

			Resource[] contentLayers = myResourceRegistry.loadResources(
					current, IResourceLoader.KIND_TYPES);
			if (contentLayers == null || contentLayers.length < 1) {
				myStatus.report(TemplatesStatus.KIND_ERROR, "Couldn't load types package `"
						+ current + "`", referer);
				continue;
			}

			for (Resource content : contentLayers) {
				TypesResolver resolver = new TypesResolver(current, content, myClasses, myStatus);
				resolver.build();
				for (String package_ : resolver.getRequired()) {
					if (!myLoadedPackages.contains(package_)) {
						queue.add(package_);
					}
				}
				loaders.add(resolver);
			}
		}

		// 2-nd stage: resolve references
		loaders.forEach(TypesResolver::resolve);

		// 3-d stage: resolve expressions
		loaders.forEach(TypesResolver::resolveExpressions);
	}
}
