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

public class TmaLexeme extends TmaNode implements TmaLexerPart {

	private final TmaIdentifier name;
	private final String type;
	private final TmaRegexp regexp;
	private final TmaReference transition;
	private final TmaLexemAttrs attrs;
	private final int priority;
	private final TmaCode code;

	public TmaLexeme(TmaIdentifier name, String type, TmaRegexp regexp, TmaReference transition,
					 Integer priority, TmaLexemAttrs attrs, TmaCode code, TextSource source, int offset,
					 int endoffset) {
		super(source, offset, endoffset);
		this.name = name;
		this.type = type;
		this.regexp = regexp;
		this.transition = transition;
		this.attrs = attrs;
		this.priority = priority != null ? priority : 0;
		this.code = code;
	}

	public TmaIdentifier getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public TmaRegexp getRegexp() {
		return regexp;
	}

	public TmaReference getTransition() {
		return transition;
	}

	public int getPriority() {
		return priority;
	}

	public TmaLexemAttrs getAttrs() {
		return attrs;
	}

	public TmaCode getCode() {
		return code;
	}

	@Override
	public void accept(AbstractVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (name != null) {
			name.accept(v);
		}
		if (regexp != null) {
			regexp.accept(v);
		}
		if (transition != null) {
			transition.accept(v);
		}
		if (attrs != null) {
			attrs.accept(v);
		}
		if (code != null) {
			code.accept(v);
		}
	}
}
