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
package org.textmapper.templates.storage;

import java.util.LinkedList;
import java.util.List;

public class ResourceRegistry {

	private final IResourceLoader[] loaders;

	public ResourceRegistry(IResourceLoader... loaders) {
		this.loaders = loaders;
	}

	public Resource[] loadResources(String qualifiedName, String kind) {
		List<Resource> loaded = null;
		int count = 0;
		for (IResourceLoader loader : loaders) {
			Resource res = loader.loadResource(qualifiedName, kind);
			if(res != null) {
				(loaded != null ? loaded : (loaded = new LinkedList<>())).add(res);
				count++;
			}
		}
		return loaded == null ? null : loaded.toArray(new Resource[count]);
	}
}
