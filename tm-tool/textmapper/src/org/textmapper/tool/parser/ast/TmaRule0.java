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

	private final TmaRhsPrefix prefix;
	private final List<ITmaRhsPart> list;
	private final TmaRuleAction action;
	private final TmaRhsSuffix suffix;
	private final TmaSyntaxProblem error;

	public TmaRule0(TmaRhsPrefix prefix, List<ITmaRhsPart> list, TmaRuleAction action, TmaRhsSuffix suffix, TextSource source,
					int line, int offset, int endoffset) {
		super(source, line, offset, endoffset);
		this.prefix = prefix;
		this.list = list;
		this.action = action;
		this.suffix = suffix;
		this.error = null;
	}

	public TmaRule0(TmaSyntaxProblem err) {
		super(err.getSource(), err.getLine(), err.getOffset(), err.getEndoffset());
		this.prefix = null;
		this.list = null;
		this.action = null;
		this.suffix = null;
		this.error = err;
	}

	public boolean hasSyntaxError() {
		return error != null;
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

	@Deprecated
	public TmaAnnotations getAnnotations() {
		// TODO -use getPrefix()
		return prefix != null ? prefix.getAnnotations() : null;
	}

	@Override
	public void accept(TmaVisitor v) {
		if (error != null) {
			v.visit(error);
			return;
		}
		if (!v.visit(this)) {
			return;
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
	}
}
