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

public class TmaLexeme extends TmaNode implements ITmaLexerPart {

	private final TmaIdentifier name;
	private final String type;
	private final TmaPattern regexp;
	private final TmaStateref transition;
	private final TmaLexemeAttrs attrs;
	private final int priority;
	private final TmaCommand code;

	public TmaLexeme(TmaIdentifier name, String type, TmaPattern regexp, TmaStateref transition,
					 Integer priority, TmaLexemeAttrs attrs, TmaCommand code, TextSource source, int offset,
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

	public TmaPattern getRegexp() {
		return regexp;
	}

	public TmaStateref getTransition() {
		return transition;
	}

	public int getPriority() {
		return priority;
	}

	public TmaLexemeAttrs getAttrs() {
		return attrs;
	}

	public TmaCommand getCode() {
		return code;
	}

	@Override
	public void accept(TmaVisitor v) {
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
