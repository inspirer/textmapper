package org.textway.templates.api.types;

public interface IFeature {

	String getName();

	IType getType();

	IMultiplicity getMultiplicity();

	boolean isReference();
}
