package net.sf.lapg.api;


public interface Annotated {

	Object getAnnotation(String name);
	void addAnnotation(String name, Object value);
}
