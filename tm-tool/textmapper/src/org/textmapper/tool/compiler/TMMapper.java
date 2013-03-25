package org.textmapper.tool.compiler;

import org.textmapper.lapg.LapgCore;
import org.textmapper.lapg.api.Grammar;
import org.textmapper.lapg.api.builder.GrammarMapper;

/**
 * evgeny, 1/29/13
 */
public class TMMapper {

	private final Grammar grammar;
	private final GrammarMapper mapper;

	public TMMapper(Grammar grammar) {
		this.grammar = grammar;
		this.mapper = LapgCore.createMapper(grammar);
	}

	public void mapAST() {

	}
}
