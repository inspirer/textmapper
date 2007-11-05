package net.sf.lapg.templates.api;

public interface IPropertyContainer {
	Object getProperty(String property);
	Object getByIndex(Object key);
}
