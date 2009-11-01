package net.sf.lapg.templates.api.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.lapg.templates.api.IProblemCollector;
import net.sf.lapg.templates.api.ITemplate;
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

	public TemplatesBundle load(String containerName, IProblemCollector collector) {
		if(sourceForPackage == null) {
			ITemplate[] templates = TemplatesBundle.parse(name, contents, null, collector);

			Map<String,List<ITemplate>> containerToTemplates = new HashMap<String, List<ITemplate>>();
			for(ITemplate t : templates) {
				String container = t.getPackage();
				List<ITemplate> list = containerToTemplates.get(container);
				if(list == null) {
					list = new LinkedList<ITemplate>();
					containerToTemplates.put(container,	list);
				}
				list.add(t);
			}

			sourceForPackage = new HashMap<String, TemplatesBundle>();
			for(Map.Entry<String, List<ITemplate>> entry : containerToTemplates.entrySet()) {
				List<ITemplate> list = entry.getValue();
				sourceForPackage.put(entry.getKey(), new TemplatesBundle(name, list.toArray(new ITemplate[list.size()])));
			}
		}
		return sourceForPackage.get(containerName);
	}
}
