/**
 * Copyright 2002-2020 Evgeny Gryaznov
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

public class TmaReportClause extends TmaNode {

	private final TmaIdentifier action;
	private final List<TmaIdentifier> flags;
	private final TmaReportAs reportAs;

	public TmaReportClause(TmaIdentifier action, List<TmaIdentifier> flags, TmaReportAs reportAs, TextSource source, int line, int offset, int endoffset) {
		super(source, line, offset, endoffset);
		this.action = action;
		this.flags = flags;
		this.reportAs = reportAs;
	}

	public TmaIdentifier getAction() {
		return action;
	}

	public List<TmaIdentifier> getFlags() {
		return flags;
	}

	public TmaReportAs getReportAs() {
		return reportAs;
	}

	@Override
	public void accept(TmaVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (action != null) {
			action.accept(v);
		}
		if (flags != null) {
			for (TmaIdentifier it : flags) {
				it.accept(v);
			}
		}
		if (reportAs != null) {
			reportAs.accept(v);
		}
	}
}
