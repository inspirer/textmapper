package org.textmapper.lapg.api;

/**
 * evgeny, 2/3/13
 */
public interface AnnotatedSourceElement extends SourceElement {

	SourceAnnotation getAnnotation(String name);

	SourceAnnotation[] getAnnotations();
}
