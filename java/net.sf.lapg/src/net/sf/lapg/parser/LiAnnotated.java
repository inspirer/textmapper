package net.sf.lapg.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.sf.lapg.api.Annotated;

public class LiAnnotated implements Annotated {
	
	private Map<String,Object> myAnnotations;

	public LiAnnotated(Map<String, Object> myAnnotations) {
		this.myAnnotations = myAnnotations != null ? myAnnotations : new HashMap<String, Object>();
	}

	@Override
	public Map<String, Object> getAnnotations() {
		return Collections.unmodifiableMap(myAnnotations);
	}
}
