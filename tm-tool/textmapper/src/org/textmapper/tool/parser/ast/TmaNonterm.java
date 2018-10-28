/**
 * Copyright 2002-2018 Evgeny Gryaznov
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

import java.util.List;
import org.textmapper.tool.parser.TMTree.TextSource;

public class TmaNonterm extends TmaNode implements ITmaGrammarPart {

	private final TmaAnnotations annotations;
	private final TmaIdentifier name;
	private final TmaNontermParams params;
	private final ITmaNontermType type;
	private final TmaReportClause defaultAction;
	private final List<TmaRule0> rules;

	public TmaNonterm(TmaAnnotations annotations, TmaIdentifier name, TmaNontermParams params, ITmaNontermType type, TmaReportClause defaultAction, List<TmaRule0> rules, TextSource source, int line, int offset, int endoffset) {
		super(source, line, offset, endoffset);
		this.annotations = annotations;
		this.name = name;
		this.params = params;
		this.type = type;
		this.defaultAction = defaultAction;
		this.rules = rules;
	}

	public TmaAnnotations getAnnotations() {
		return annotations;
	}

	public TmaIdentifier getName() {
		return name;
	}

	public TmaNontermParams getParams() {
		return params;
	}

	public ITmaNontermType getType() {
		return type;
	}

	public TmaReportClause getDefaultAction() {
		return defaultAction;
	}

	public List<TmaRule0> getRules() {
		return rules;
	}

	@Override
	public void accept(TmaVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (annotations != null) {
			annotations.accept(v);
		}
		if (name != null) {
			name.accept(v);
		}
		if (params != null) {
			params.accept(v);
		}
		if (type != null) {
			type.accept(v);
		}
		if (defaultAction != null) {
			defaultAction.accept(v);
		}
		if (rules != null) {
			for (TmaRule0 it : rules) {
				it.accept(v);
			}
		}
	}
}
