package net.sf.lapg.parser.ast;

import java.util.List;

import net.sf.lapg.parser.LapgTree.TextSource;

public class AstGroupsSelector extends Node implements AstLexerPart {

	private final List<Integer> groups;

	public AstGroupsSelector(List<Integer> groups, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.groups = groups;
	}

	public List<Integer> getGroups() {
		return groups;
	}
}
