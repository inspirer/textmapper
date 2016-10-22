/**
 * Copyright 2002-2016 Evgeny Gryaznov
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
package org.textmapper.tool.compiler;

import org.textmapper.lapg.api.*;
import org.textmapper.lapg.api.rule.*;
import org.textmapper.lapg.common.RuleUtil;
import org.textmapper.lapg.util.NonterminalUtil;
import org.textmapper.lapg.util.RhsUtil;

public class TMEventMapper {

	private final Grammar grammar;
	private final ProcessingStatus status;

	public TMEventMapper(Grammar grammar, ProcessingStatus status) {
		this.grammar = grammar;
		this.status = status;
	}

	public void deriveTypes() {
		for (Symbol sym : grammar.getSymbols()) {
			if (!(sym instanceof Nonterminal)) continue;
			assignRoles(((Nonterminal) sym).getDefinition());
		}
		for (Rule rule : grammar.getRules()) {
			assignRangeType(rule);
		}
	}

	private void assignRangeType(Rule rule) {
		RhsSequence seq = rule.getSource();
		if (seq.getName() != null) {
			TMDataUtil.putRangeType(rule, seq.getName());
			return;
		}

		if (seq.getParts().length > 0 &&
				TMDataUtil.hasProperty(seq.getParts()[0], "noast")) {
			return;
		}

		Nonterminal n = rule.getLeft();
		if (n instanceof Lookahead) return;
		if (!TMDataUtil.hasProperty(n, "ast")) {
			if (n.getDefinition() instanceof RhsList
					&& ((RhsList) n.getDefinition()).getCustomInitialElement() == null
					|| NonterminalUtil.isOptional(n)
					|| TMDataUtil.hasProperty(n, "_set")
					|| TMDataUtil.hasProperty(n, "noast")) {
				return;
			}
		}

		if (n.getTemplate() != null) n = n.getTemplate();
		TMDataUtil.putRangeType(rule, n.getName());
	}

	private void assignRoles(RhsPart part) {
		switch (part.getKind()) {
			case Assignment: {
				RhsAssignment a = (RhsAssignment) part;
				RhsSymbol sym = RuleUtil.getAssignmentSymbol(a);
				if (sym != null) {
					TMDataUtil.putRole(sym, a.getName());
				}
				return;
			}
			case Ignored:
			case Symbol:
			case Set:
			case StateMarker:
				// cannot contain aliases
				return;
			case Optional:
			case Cast:
			case Choice:
			case Sequence:
			case Unordered:
			case List:
				final Iterable<RhsPart> children = RhsUtil.getChildren(part);
				if (children == null) return;

				for (RhsPart child : children) {
					assignRoles(child);
				}
				return;
			case Conditional:
				throw new UnsupportedOperationException();
			default:
				throw new IllegalStateException();
		}
	}
}
