package org.textway.templates.api.types;

import org.textway.templates.api.SourceElement;

public interface ITypesRegistry {

	IClass getClass(String qualifiedName, SourceElement referer);
}
