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
package org.textmapper.lapg.util;

import org.textmapper.lapg.api.Nonterminal;
import org.textmapper.lapg.api.rule.*;

import java.util.Arrays;

public class NonterminalUtil {

	public static Iterable<RhsSequence> getRules(Nonterminal n) {
		RhsPart definition = n.getDefinition();
		if (!(definition instanceof RhsChoice)) {
			throw new IllegalStateException();
		}

		RhsPart[] rules = ((RhsChoice) definition).getParts();
		for (RhsPart p : rules) {
			if (!(p instanceof RhsSequence)) throw new IllegalStateException();
		}
		RhsSequence[] result = new RhsSequence[rules.length];
		System.arraycopy(rules, 0, result, 0, rules.length);
		return Arrays.asList(result);
	}

	public static boolean isOptional(Nonterminal n) {
		if (n.getDefinition() instanceof RhsList) return false;

		int refs = 0;
		boolean hasEmpty = false;
		for (RhsSequence rule : NonterminalUtil.getRules(n)) {
			if (RhsUtil.isEmpty(rule)) {
				hasEmpty = true;
				continue;
			}
			RhsPart p = RhsUtil.unwrap(rule);

			if (p instanceof RhsOptional) {
				hasEmpty = true;
				p = RhsUtil.unwrapOpt(p);
			}
			if (p instanceof RhsSymbol) {
				refs++;
			} else {
				return false;
			}
		}
		return refs == 1 && hasEmpty;
	}

	public static boolean isList(Nonterminal n) {
		return n.getDefinition() instanceof RhsList;
	}
}
