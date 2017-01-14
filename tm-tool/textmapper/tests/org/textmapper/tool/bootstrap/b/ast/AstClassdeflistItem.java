/**
 * Copyright 2002-2017 Evgeny Gryaznov
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
package org.textmapper.tool.bootstrap.b.ast;

import org.textmapper.tool.bootstrap.b.SampleBTree.TextSource;

public class AstClassdeflistItem extends AstNode {

	private final AstClassdef classdef;
	private final String identifier;

	public AstClassdeflistItem(AstClassdef classdef, String identifier, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.classdef = classdef;
		this.identifier = identifier;
	}

	public AstClassdef getClassdef() {
		return classdef;
	}

	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void accept(AstVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (classdef != null) {
			classdef.accept(v);
		}
	}
}
