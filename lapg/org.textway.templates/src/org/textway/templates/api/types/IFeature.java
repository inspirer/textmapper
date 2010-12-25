package org.textway.templates.api.types;

public interface IFeature {

	String getName();

	IType getTarget();

	IMultiplicity getMultiplicity();

	boolean isReference();

	Object getDefaultValue(); 
}
