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
 * evgeny, 8/7/12
 */
public class AstRuleNestedListWithSeparator extends AstNode implements AstRuleSymbolRef {

	private final List<AstRulePart> ruleParts;
	private final List<AstReference> separator;
	private final boolean atLeastOne;

	public AstRuleNestedListWithSeparator(List<AstRulePart> ruleParts, List<AstReference> separator, boolean atLeastOne, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.ruleParts = ruleParts;
		this.separator = separator;
		this.atLeastOne = atLeastOne;
	}

	public List<AstRulePart> getRuleParts() {
		return ruleParts;
	}

	public List<AstReference> getSeparator() {
		return separator;
	}

	public boolean isAtLeastOne() {
		return atLeastOne;
	}

	@Override
	public void accept(AbstractVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (ruleParts != null) {
			for (AstRulePart rulePart : ruleParts) {
				rulePart.accept(v);
			}
		}
		if (separator != null) {
			for (AstReference ref : separator) {
				ref.accept(v);
			}
		}
	}
}
