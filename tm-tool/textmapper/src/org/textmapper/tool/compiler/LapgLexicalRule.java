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
package org.textmapper.tool.compiler;

import org.textmapper.lapg.api.LexerState;
import org.textmapper.tool.parser.ast.AstLexeme;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Gryaznov Evgeny, 9/11/12
 */
public class LapgLexicalRule {
	private final AstLexeme astLexeme;
	private final LapgStateTransitionSwitch transitions;
	private final List<LexerState> applicableInStates;

	public LapgLexicalRule(AstLexeme astLexeme, LapgStateTransitionSwitch transitions, List<LexerState> applicableInStates) {
		this.astLexeme = astLexeme;
		this.transitions = transitions;
		this.applicableInStates = applicableInStates;
	}

	public AstLexeme getLexeme() {
		return astLexeme;
	}

	public LapgStateTransitionSwitch getTransitions() {
		return transitions;
	}

	public List<LexerState> getApplicableInStates() {
		return applicableInStates;
	}

	public boolean canBeClassFor(LapgLexicalRule l) {
		if(applicableInStates.size() != l.getApplicableInStates().size()) {
			return false;
		}
		Collection<LexerState> applicableInStatesSet = applicableInStates.size() > 4 ? new HashSet<LexerState>(applicableInStates) : applicableInStates;
		if (!(applicableInStatesSet.containsAll(l.getApplicableInStates()))) {
			return false;
		}
		return this.transitions == null ? l.getTransitions() == null : this.transitions.equals(l.getTransitions());
	}

}
