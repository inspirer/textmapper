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
package org.textmapper.lapg.api.builder;

import org.textmapper.lapg.api.Nonterminal;
import org.textmapper.lapg.api.ast.AstField;
import org.textmapper.lapg.api.ast.AstType;
import org.textmapper.lapg.api.rule.RhsSequence;
import org.textmapper.lapg.api.rule.RhsSymbol;

/**
 * evgeny, 3/25/13
 */
public interface GrammarMapper {

	void map(Nonterminal symbol, AstType type);

	void map(RhsSequence seq, AstField field, AstType subType, boolean isAddition);

	void map(RhsSymbol symbol, AstField field, Object value, boolean isAddition);
}
