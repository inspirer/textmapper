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
package org.textmapper.tool.parser.ast;

import org.textmapper.tool.parser.LapgTree.TextSource;

import java.util.List;

public class AstInstance extends AstNode implements AstExpression {

	private final AstName className;
	private final List<AstNamedEntry> mapEntries;

	public AstInstance(AstName className, List<AstNamedEntry> mapEntries, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.className = className;
		this.mapEntries = mapEntries;
	}

	public AstName getClassName() {
		return className;
	}

	public List<AstNamedEntry> getEntries() {
		return mapEntries;
	}

	public void accept(AbstractVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (className != null) {
			className.accept(v);
		}
		if (mapEntries != null) {
			for (AstNamedEntry n : mapEntries) {
				n.accept(v);
			}
		}
	}
}
