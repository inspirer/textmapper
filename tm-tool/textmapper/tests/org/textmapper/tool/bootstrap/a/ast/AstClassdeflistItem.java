/**
 * Copyright 2002-2022 Evgeny Gryaznov
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
package org.textmapper.tool.bootstrap.a.ast;

import org.textmapper.tool.bootstrap.a.SampleATree.TextSource;

public class AstClassdeflistItem extends AstNode {

	private final AstClassdef classdef;

	public AstClassdeflistItem(AstClassdef classdef, TextSource source, int line, int offset, int column, int endline, int endoffset, int endcolumn) {
		super(source, line, offset, column, endline, endoffset, endcolumn);
		this.classdef = classdef;
	}

	public AstClassdef getClassdef() {
		return classdef;
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
