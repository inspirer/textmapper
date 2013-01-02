/**
 * Copyright 2002-2013 Evgeny Gryaznov
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.textmapper.templates.api.TemplatesStatus;
import org.textmapper.templates.storage.Resource;

/**
 * In-memory template loader.
 */
public class StringTemplateLoader implements IBundleLoader {

	private final Resource resource;
	private Map<String,TemplatesBundle> sourceForPackage;

	public StringTemplateLoader(Resource resource) {
		this.resource = resource;
	}

	public TemplatesBundle[] load(String bundleName, TemplatesStatus status) {
		if(sourceForPackage == null) {
			TemplatesBundle compositeBundle = TemplatesBundle.parse(resource, null, status);

			Map<String,List<IBundleEntity>> bundleToTemplates = new HashMap<String, List<IBundleEntity>>();
			for(IBundleEntity t : compositeBundle.getEntities()) {
				String tbundle = t.getPackage();
				List<IBundleEntity> list = bundleToTemplates.get(tbundle);
				if(list == null) {
					list = new LinkedList<IBundleEntity>();
					bundleToTemplates.put(tbundle,	list);
				}
				list.add(t);
			}

			sourceForPackage = new HashMap<String, TemplatesBundle>();
			for(Map.Entry<String, List<IBundleEntity>> entry : bundleToTemplates.entrySet()) {
				List<IBundleEntity> list = entry.getValue();
				sourceForPackage.put(entry.getKey(), new TemplatesBundle(resource, list.toArray(new IBundleEntity[list.size()])));
			}
		}
		TemplatesBundle result = sourceForPackage.get(bundleName);
		return result == null ? null : new TemplatesBundle[] { result };
	}
}
