package org.textmapper.lapg.api.rule;

import org.textmapper.lapg.api.ast.AstEnumMember;
import org.textmapper.lapg.api.ast.AstField;

/**
 * evgeny, 2/25/13
 */
public interface RhsMapping {

	AstField getField();

	/**
	 *  Only for fields with AstEnum type.
	 */
	AstEnumMember getValue();

	boolean isAddition();
}
