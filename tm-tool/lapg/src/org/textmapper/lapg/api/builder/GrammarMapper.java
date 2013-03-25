package org.textmapper.lapg.api.builder;

import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.ast.AstEnumMember;
import org.textmapper.lapg.api.ast.AstField;
import org.textmapper.lapg.api.ast.AstType;
import org.textmapper.lapg.api.rule.RhsSymbol;

/**
 * evgeny, 3/25/13
 */
public interface GrammarMapper {

	void map(Symbol symbol, AstType type);

	void map(RhsSymbol symbol, AstField field, AstEnumMember value, boolean isAddition);
}
