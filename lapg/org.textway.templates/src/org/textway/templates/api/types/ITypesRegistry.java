package org.textway.templates.api.types;

import org.textway.templates.bundle.ILocatedEntity;

public interface ITypesRegistry {
	
	IClass loadClass(String qualifiedName, ILocatedEntity referer);
}
