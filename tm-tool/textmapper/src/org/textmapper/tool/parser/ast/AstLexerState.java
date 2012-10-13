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

/**
 * Gryaznov Evgeny, 9/9/12
 */
public class AstLexerState extends AstNode {

	private AstIdentifier name;
	private AstReference defaultTransition;

	public AstLexerState(AstIdentifier name, AstReference defaultTransition, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.name = name;
		this.defaultTransition = defaultTransition;
	}

	public AstIdentifier getName() {
		return name;
	}

	public AstReference getDefaultTransition() {
		return defaultTransition;
	}

	@Override
	public void accept(AbstractVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (name != null) {
			name.accept(v);
		}
		if (defaultTransition != null) {
			defaultTransition.accept(v);
		}
	}
}
