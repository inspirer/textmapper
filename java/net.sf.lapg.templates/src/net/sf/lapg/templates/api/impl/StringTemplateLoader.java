package net.sf.lapg.templates.api.impl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.sf.lapg.templates.api.ITemplate;
import net.sf.lapg.templates.api.ITemplateLoader;
import net.sf.lapg.templates.api.ITemplatesFacade;
import net.sf.lapg.templates.api.TemplateSource;

/**
 * In-memory template loader. 
 */
public class StringTemplateLoader implements ITemplateLoader {

	private final String name;
	private final String contents;
	
	private Map<String,TemplateSource> sourceForPackage;

	public StringTemplateLoader(String name, String contents) {
		this.name = name;
		this.contents = contents;
	}

	public TemplateSource load(String containerName, ITemplatesFacade facade) {
		if(sourceForPackage == null) {
			TemplateSource cached = TemplateSource.buildSource(name, contents, null, facade);
			sourceForPackage = new HashMap<String, TemplateSource>();
			
			Map<String,List<ITemplate>> temp = new HashMap<String, List<ITemplate>>();
			for(ITemplate t : cached.getTemplates()) {
				String container = t.getPackage();
				List<ITemplate> list = temp.get(container);
				if(list == null) {
					list = new LinkedList<ITemplate>();
					temp.put(container,	list);
				}
				list.add(t);
			}
			
			for(Map.Entry<String, List<ITemplate>> entry : temp.entrySet()) {
				List<ITemplate> list = entry.getValue();
				sourceForPackage.put(entry.getKey(), new TemplateSource(name, list.toArray(new ITemplate[list.size()])));
			}
		}
		return sourceForPackage.get(containerName);
	}
}
