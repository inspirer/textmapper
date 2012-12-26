package org.textmapper.lapg.builder;

import org.textmapper.lapg.api.rule.RhsChoice;
import org.textmapper.lapg.api.rule.RhsPart;

import java.util.ArrayList;
import java.util.List;

/**
 * evgeny, 12/25/12
 */
class LiRootRhsChoice implements RhsChoice {

	private List<RhsPart> rules = new ArrayList<RhsPart>();

	LiRootRhsChoice() {
	}

	void addRule(RhsPart rule) {
		rules.add(rule);
	}

	@Override
	public RhsPart[] getParts() {
		return rules.toArray(new RhsPart[rules.size()]);
	}

	@Override
	public Object structuralNode() {
		return this;
	}
}
