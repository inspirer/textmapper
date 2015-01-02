/**
 * Copyright 2002-2014 Evgeny Gryaznov
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

public class TmaRule0 extends TmaNode {

	private final ITmaPredicateExpression predicate;
	private final TmaRhsPrefix prefix;
	private final List<ITmaRhsPart> list;
	private final TmaRuleAction action;
	private final TmaRhsSuffix suffix;
	private final TmaSyntaxProblem error;

	public TmaRule0(ITmaPredicateExpression predicate, TmaRhsPrefix prefix, List<ITmaRhsPart> list, TmaRuleAction action, TmaRhsSuffix suffix, TmaSyntaxProblem error, TextSource source, int line, int offset, int endoffset) {
		super(source, line, offset, endoffset);
		this.predicate = predicate;
		this.prefix = prefix;
		this.list = list;
		this.action = action;
		this.suffix = suffix;
		this.error = error;
	}

	public ITmaPredicateExpression getPredicate() {
		return predicate;
	}

	public TmaRhsPrefix getPrefix() {
		return prefix;
	}

	public List<ITmaRhsPart> getList() {
		return list;
	}

	public TmaRuleAction getAction() {
		return action;
	}

	public TmaRhsSuffix getSuffix() {
		return suffix;
	}

	public TmaSyntaxProblem getError() {
		return error;
	}

	@Override
	public void accept(TmaVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (predicate != null) {
			predicate.accept(v);
		}
		if (prefix != null) {
			prefix.accept(v);
		}
		if (list != null) {
			for (ITmaRhsPart it : list) {
				it.accept(v);
			}
		}
		if (action != null) {
			action.accept(v);
		}
		if (suffix != null) {
			suffix.accept(v);
		}
		if (error != null) {
			error.accept(v);
		}
	}
}
