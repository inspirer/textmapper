package org.textway.templates.api.types;

public interface IFeature {

	String getName();

	IType getTarget();

	IType getType();

	IMultiplicity[] getMultiplicities();

	boolean isReference();

	Object getDefaultValue(); 
}
