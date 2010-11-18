package org.textway.templates.api.types;

import org.textway.templates.api.IInstanceObject;

public interface IFeature {

	String getName();

	IType getType();

	IMultiplicity getMultiplicity();

	boolean isReference();

	IInstanceObject getDefaultValue(); 
}
