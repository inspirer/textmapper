/**
 * Copyright 2002-2012 Evgeny Gryaznov
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
package org.textmapper.lapg.parser.ast;

import org.textmapper.lapg.parser.LapgTree.TextSource;

import java.util.List;

/**
 * evgeny, 8/2/12
 */
public class AstRuleNestedNonTerm extends AstNode implements AstRuleSymbolRef {

	private final List<AstRule> rules;

	public AstRuleNestedNonTerm(List<AstRule> rules, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.rules = rules;
	}

	public List<AstRule> getRules() {
		return rules;
	}

	public void accept(AbstractVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (rules != null) {
			for (AstRule rule : rules) {
				rule.accept(v);
			}
		}
	}
}
