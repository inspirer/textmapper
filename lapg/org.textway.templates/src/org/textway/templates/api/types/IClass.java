package org.textway.templates.api.types;

import java.util.Collection;

public interface IClass extends IType {

	String getName();

	String getQualifiedName();

	Collection<IClass> getExtends();

	Collection<IFeature> getFeatures();

	IFeature getFeature(String name);
}
