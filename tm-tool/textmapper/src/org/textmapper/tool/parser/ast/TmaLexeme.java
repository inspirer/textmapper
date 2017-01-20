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
package org.textmapper.tool.parser.ast;

import org.textmapper.tool.parser.TMTree.TextSource;

public class TmaLexeme extends TmaNode implements ITmaLexerPart {

	private final TmaIdentifier name;
	private final TmaRawType rawType;
	private final TmaPattern pattern;
	private final Integer priority;
	private final TmaLexemeAttrs attrs;
	private final TmaCommand command;

	public TmaLexeme(TmaIdentifier name, TmaRawType rawType, TmaPattern pattern, Integer priority, TmaLexemeAttrs attrs, TmaCommand command, TextSource source, int line, int offset, int endoffset) {
		super(source, line, offset, endoffset);
		this.name = name;
		this.rawType = rawType;
		this.pattern = pattern;
		this.priority = priority;
		this.attrs = attrs;
		this.command = command;
	}

	public TmaIdentifier getName() {
		return name;
	}

	public TmaRawType getRawType() {
		return rawType;
	}

	public TmaPattern getPattern() {
		return pattern;
	}

	public Integer getPriority() {
		return priority;
	}

	public TmaLexemeAttrs getAttrs() {
		return attrs;
	}

	public TmaCommand getCommand() {
		return command;
	}

	@Override
	public void accept(TmaVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (name != null) {
			name.accept(v);
		}
		if (rawType != null) {
			rawType.accept(v);
		}
		if (pattern != null) {
			pattern.accept(v);
		}
		if (attrs != null) {
			attrs.accept(v);
		}
		if (command != null) {
			command.accept(v);
		}
	}
}
