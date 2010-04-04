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
package net.sf.lapg.templates.api.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.lapg.templates.api.IBundleEntity;
import net.sf.lapg.templates.api.IProblemCollector;
import net.sf.lapg.templates.api.IBundleLoader;
import net.sf.lapg.templates.api.TemplatesBundle;

/**
 * In-memory template loader.
 */
public class StringTemplateLoader implements IBundleLoader {

	private final String name;
	private final String contents;

	private Map<String,TemplatesBundle> sourceForPackage;

	public StringTemplateLoader(String name, String contents) {
		this.name = name;
		this.contents = contents;
	}

	public TemplatesBundle load(String bundleName, IProblemCollector collector) {
		if(sourceForPackage == null) {
			TemplatesBundle compositeBundle = TemplatesBundle.parse(name, contents, null, collector);

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
				sourceForPackage.put(entry.getKey(), new TemplatesBundle(name, list.toArray(new IBundleEntity[list.size()])));
			}
		}
		return sourceForPackage.get(bundleName);
	}
}
