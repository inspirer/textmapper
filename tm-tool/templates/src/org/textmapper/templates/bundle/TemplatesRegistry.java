/**
 * Copyright 2002-2017 Evgeny Gryaznov
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
package org.textmapper.templates.bundle;

import java.util.*;

import org.textmapper.templates.api.SourceElement;
import org.textmapper.templates.api.TemplatesStatus;
import org.textmapper.templates.api.types.ITypesRegistry;

public class TemplatesRegistry {

	private final TemplatesStatus status;
	private final Set<String> loadedBundles;
	private final Map<String, IBundleEntity> entities;
	private final IBundleLoader[] loaders;
	private final ITypesRegistry typesRegistry;

	public TemplatesRegistry(TemplatesStatus status, ITypesRegistry typesRegistry, IBundleLoader... loaders) {
		this.status = status;
		this.typesRegistry = typesRegistry;
		this.entities = new HashMap<>();
		this.loadedBundles = new HashSet<>();
		this.loaders = loaders;

		if (loaders == null || loaders.length < 1) {
			throw new IllegalArgumentException("no loaders provided");
		}
	}

	private TemplatesBundle[] getBundleContents(String bundleName) {
		List<TemplatesBundle> result = new LinkedList<>();
		for (IBundleLoader loader : loaders) {
			TemplatesBundle[] sources = loader.load(bundleName, status);
			if (sources != null) {
				Collections.addAll(result, sources);
			}
		}
		return result.size() > 0 ? result.toArray(new TemplatesBundle[result.size()]) : null;
	}

	private void loadBundle(SourceElement referer, String bundleName) {
		if (loadedBundles.contains(bundleName)) {
			return;
		}

		TemplatesBundle[] contents = getBundleContents(bundleName);
		if (contents == null) {
			status.report(TemplatesStatus.KIND_ERROR, "Couldn't load package `" + bundleName + "`", referer);
			return;
		}

		Map<String, IBundleEntity> nameToEntity = new HashMap<>();
		Set<String> seenNames = new HashSet<>();
		for (int i = contents.length - 1; i >= 0; i--) {
			IBundleEntity[] loaded = contents[i].getEntities();
			if (loaded == null || loaded.length == 0) {
				status.report(TemplatesStatus.KIND_ERROR, "Couldn't get templates from " + contents[i].getName(), referer);
				return;
			}

			seenNames.clear();
			for (IBundleEntity t : loaded) {
				String name = t.getName();
				IBundleEntity base = nameToEntity.get(name);
				if (seenNames.contains(name)) {
					String baseKind = base != null ? kindToString(base.getKind()) : "Element";
					status.report(TemplatesStatus.KIND_ERROR, baseKind + " `" + bundleName + "." + name + "` was already defined", t);
				} else {
					if (base != null ) {
						if (base.getKind() != t.getKind() || !base.getSignature().equals(t.getSignature())) {
							status.report(TemplatesStatus.KIND_ERROR, kindToString(t.getKind()) + " `" + t.toString()
									+ "` is not compatible with base " + kindToString(base.getKind()).toLowerCase()
									+ " `" + base.toString() + "`", t);
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

	public IBundleEntity loadEntity(String qualifiedName, int kind, SourceElement referer) {
		int lastDot = qualifiedName.lastIndexOf('.');
		if (lastDot == -1) {
			status.report(TemplatesStatus.KIND_ERROR, "Fully qualified name should contain dot.", referer);
			return null;
		}

		String templatePackage = qualifiedName.substring(0, lastDot);
		loadBundle(referer, templatePackage);

		String resolvedName = qualifiedName;

		IBundleEntity t = entities.get(resolvedName);
		if (t == null || kind != IBundleEntity.KIND_ANY && t.getKind() != kind) {
			status.report(TemplatesStatus.KIND_ERROR,
					kindToString(kind) + " `" + resolvedName + "` was not found in package `" + templatePackage + "`",
					referer);
			t = null;
		}
		return t;
	}

	public TemplatesStatus getStatus() {
		return status;
	}

	public ITypesRegistry getTypesRegistry() {
		return typesRegistry;
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
