/**
 * Copyright 2002-2017 Evgeny Gryaznov
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
package org.textmapper.lapg.api;

import org.textmapper.lapg.api.rule.LookaheadPredicate;

/**
 * Lookahead is a special kind of nonterminal that accepts an empty string,
 * but is able to survive reduce/reduce conflicts. When two or more lookahead
 * nonterminals can be reduced in the same state, their lookahead predicates
 * are used to determine which one should be reduced.
 */
public interface Lookahead extends Nonterminal {

	LookaheadPredicate[] getLookaheadPredicates();

	String asString();
}
