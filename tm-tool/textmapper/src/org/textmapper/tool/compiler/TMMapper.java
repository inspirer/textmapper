package org.textmapper.tool.compiler;

import org.textmapper.lapg.LapgCore;
import org.textmapper.lapg.api.Grammar;
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.UserDataHolder;
import org.textmapper.lapg.api.builder.AstBuilder;
import org.textmapper.lapg.api.builder.GrammarMapper;

import java.util.Map;

/**
 * evgeny, 1/29/13
 */
public class TMMapper {

	private final Grammar grammar;
	private final GrammarMapper mapper;
	private final AstBuilder builder;

	public TMMapper(Grammar grammar) {
		this.grammar = grammar;
		this.mapper = LapgCore.createMapper(grammar);
		this.builder = LapgCore.createAstBuilder();
	}

	public void deriveAST() {
	}


	private static boolean is(UserDataHolder o, String name) {
		final Map<String, Object> annotations = TMDataUtil.getAnnotations(o);
		if (annotations == null) {
			return false;
		}
		final Object o1 = annotations.get(name);
		return o1 instanceof Boolean ? (Boolean) o1 : false;
	}

	private static Symbol getExtends(Symbol s) {
		final Map<String, Object> annotations = TMDataUtil.getAnnotations(s);
		if (annotations == null) {
			return null;
		}
		final Object o1 = annotations.get("extends");
		return o1 instanceof Symbol ? (Symbol) o1 : null;

	}
}
