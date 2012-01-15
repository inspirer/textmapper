/**
 * Copyright 2002-2012 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.textway.lapg.parser.ast;

import org.textway.lapg.parser.LapgTree.TextSource;

import java.util.List;

public class AstGroupsSelector extends AstNode implements AstLexerPart {

	private final List<Integer> groups;

	public AstGroupsSelector(List<Integer> groups, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.groups = groups;
	}

	public List<Integer> getGroups() {
		return groups;
	}

	public void accept(AbstractVisitor v) {
		v.visit(this);
	}
}
