package org.textmapper.lapg.api.rule;

/**
 * evgeny, 1/3/13
 */
public interface RhsList extends RhsRoot {

	RhsSequence getElement();

	RhsSequence getCustomInitialElement();

	RhsPart getSeparator();

	boolean isNonEmpty();

	boolean isRightRecursive();

	RhsSequence[] asRules();
}
