package org.textmapper.lapg.api.builder;

import org.textmapper.lapg.api.Nonterminal;
import org.textmapper.lapg.api.ast.AstField;
import org.textmapper.lapg.api.ast.AstType;
import org.textmapper.lapg.api.rule.RhsSequence;
import org.textmapper.lapg.api.rule.RhsSymbol;

/**
 * evgeny, 3/25/13
 */
public interface GrammarMapper {

	void map(Nonterminal symbol, AstType type);

	void map(RhsSequence seq, AstField field, AstType subType, boolean isAddition);

	void map(RhsSymbol symbol, AstField field, Object value, boolean isAddition);
}
