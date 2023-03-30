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
package org.textmapper.tool.parser.ast;

import java.util.List;
import org.textmapper.tool.parser.TMTree.TextSource;

public class TmaRule0 extends TmaNode {

	private final ITmaPredicateExpression predicate;
	private final List<ITmaRhsPart> list;
	private final TmaRhsSuffix suffix;
	private final TmaReportClause action;
	private final TmaSyntaxProblem error;

	public TmaRule0(ITmaPredicateExpression predicate, List<ITmaRhsPart> list, TmaRhsSuffix suffix, TmaReportClause action, TmaSyntaxProblem error, TextSource source, int line, int offset, int endoffset) {
		super(source, line, offset, endoffset);
		this.predicate = predicate;
		this.list = list;
		this.suffix = suffix;
		this.action = action;
		this.error = error;
	}

	public ITmaPredicateExpression getPredicate() {
		return predicate;
	}

	public List<ITmaRhsPart> getList() {
		return list;
	}

	public TmaRhsSuffix getSuffix() {
		return suffix;
	}

	public TmaReportClause getAction() {
		return action;
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
		if (list != null) {
			for (ITmaRhsPart it : list) {
				it.accept(v);
			}
		}
		if (suffix != null) {
			suffix.accept(v);
		}
		if (action != null) {
			action.accept(v);
		}
		if (error != null) {
			error.accept(v);
		}
	}
}
