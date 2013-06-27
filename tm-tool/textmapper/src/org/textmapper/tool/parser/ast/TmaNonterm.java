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

import java.util.List;

public class TmaNonterm extends TmaNode implements TmaGrammarPart {

	private final TmaIdentifier name;
	private final TmaNontermType type;
	private final List<TmaRule0> rules;
	private final TmaAnnotations annotations;

	public TmaNonterm(TmaIdentifier name, TmaNontermType type, List<TmaRule0> rules,
					  TmaAnnotations annotations, TextSource source, int offset,
					  int endoffset) {
		super(source, offset, endoffset);
		this.name = name;
		this.type = type;
		this.rules = rules;
		this.annotations = annotations;
	}

	public TmaIdentifier getName() {
		return name;
	}

	public TmaNontermType getType() {
		return type;
	}

	public List<TmaRule0> getRules() {
		return rules;
	}

	public TmaAnnotations getAnnotations() {
		return annotations;
	}

	public void accept(AbstractVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (annotations != null) {
			annotations.accept(v);
		}
		if (name != null) {
			name.accept(v);
		}
		if (rules != null) {
			for (TmaRule0 r : rules) {
				r.accept(v);
			}
		}
	}
}
