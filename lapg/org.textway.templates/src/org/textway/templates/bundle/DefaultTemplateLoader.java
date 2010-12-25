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
package org.textway.templates.bundle;

import org.textway.templates.api.TemplatesStatus;
import org.textway.templates.storage.IResourceLoader;
import org.textway.templates.storage.Resource;
import org.textway.templates.storage.ResourceRegistry;

/**
 * Loads templates from specified folders;
 */
public class DefaultTemplateLoader implements IBundleLoader {
	private ResourceRegistry resources;

	public DefaultTemplateLoader(ResourceRegistry resources) {
		this.resources = resources;
	}

	public TemplatesBundle[] load(String bundleName, TemplatesStatus status) {
		Resource[] loaded = resources.loadResources(bundleName, IResourceLoader.KIND_TEMPLATE);
		if (loaded == null) {
			return null;
		}
		TemplatesBundle[] result = new TemplatesBundle[loaded.length];
		for(int i = 0; i < loaded.length; i++) {
			Resource resource = loaded[i];
			result[i] = TemplatesBundle.parse(resource, bundleName, status);
		}
		return result;
	}
}
