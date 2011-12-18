package org.textway.lapg.parser;

import org.textway.lapg.api.SourceElement;

public interface TextSourceElement extends SourceElement {

	String getResourceName();

	int getOffset();

	int getEndOffset();

	int getLine();

	String getText();
}
