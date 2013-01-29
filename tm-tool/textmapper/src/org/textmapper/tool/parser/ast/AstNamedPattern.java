/**
 * Copyright 2002-2013 Evgeny Gryaznov
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

import org.textmapper.tool.parser.TMTree.TextSource;

/**
 * Gryaznov Evgeny, 6/23/11
 */
public class AstNamedPattern extends AstNode implements AstLexerPart {

	private String name;
	private AstRegexp regexp;

	public AstNamedPattern(String name, AstRegexp regexp, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.name = name;
		this.regexp = regexp;
	}

	public String getName() {
		return name;
	}

	public AstRegexp getRegexp() {
		return regexp;
	}

	public void accept(AbstractVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (regexp != null) {
			regexp.accept(v);
		}
	}
}
