package org.textway.lapg.builder;

import org.textway.lapg.api.builder.GrammarBuilder;

public class GrammarFacade {

	public static GrammarBuilder createBuilder() {
		return new LiGrammarBuilder();
	}

}
