package org.textmapper.lapg.api.rule;

import org.textmapper.lapg.api.ast.AstField;

/**
 * evgeny, 2/25/13
 */
public interface RhsMapping {

	AstField getField();

	/**
	 * Only for fields with AstEnum or primitive type.
	 * Can be instanceof AstEnumMember, Integer, Boolean or String.
	 */
	Object getValue();

	/**
	 * Only for fields with AstList type.
	 */
	boolean isAddition();
}
