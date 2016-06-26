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
package org.textmapper.lapg.lalr;

import org.textmapper.lapg.api.Lookahead;
import org.textmapper.lapg.api.LookaheadRule;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExplicitLookaheadBuilder {

	int rules;
	int resolutionRules = 0;

	Map<Set<Lookahead>, LookaheadRule> resolutionMap = new HashMap<>();

	public ExplicitLookaheadBuilder(int rules) {
		this.rules = rules;
	}

	boolean isResolutionRule(int rule) {
		return rule >= this.rules;
	}

	LookaheadRule[] getRules() {
		return new LookaheadRule[0];
	}
}
