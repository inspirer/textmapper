package org.textway.templates.api.types;

import java.util.Collection;

public interface IClass extends IType {

	String getName();

	Collection<IClass> getExtends();

	Collection<IFeature> getFeatures();
}
