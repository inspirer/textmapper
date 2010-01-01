package net.sf.lapg.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sf.lapg.api.Annotated;

public class LiAnnotated implements Annotated {

	private static final Map<String,Object> EMPTY_ANN = Collections.<String,Object>emptyMap();

	private Map<String,Object> myAnnotations;

	public LiAnnotated(Map<String, Object> myAnnotations) {
		this.myAnnotations = myAnnotations != null ? myAnnotations : EMPTY_ANN;
	}

	@Override
	public void addAnnotation(String name, Object value) {
		if(myAnnotations == EMPTY_ANN) {
			myAnnotations = new HashMap<String, Object>();
		}
		myAnnotations.put(name, value);
	}

	@Override
	public Object getAnnotation(String name) {
		return myAnnotations.get(name);
	}
}
