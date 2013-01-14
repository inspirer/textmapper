/**
 * Copyright 2002-2013 Evgeny Gryaznov
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

import org.textmapper.lapg.api.*;
import org.textmapper.lapg.api.regex.RegexPart;
import org.textmapper.lapg.api.rule.*;

import java.util.Collection;

public interface GrammarBuilder {

	Terminal addTerminal(String name, String type, SourceElement origin);

	Terminal addSoftTerminal(String name, Terminal softClass, SourceElement origin);

	Nonterminal addNonterminal(String name, String type, SourceElement origin);

	Terminal getEoi();


	NamedPattern addPattern(String name, RegexPart regexp, SourceElement origin);

	LexerState addState(String name, SourceElement origin);

	LexicalRule addLexicalRule(int kind, Terminal sym, RegexPart regexp, Iterable<LexerState> states, int priority, LexicalRule classLexicalRule, SourceElement origin);


	RhsSymbol symbol(String alias, Symbol sym, Collection<Terminal> unwanted, SourceElement origin);

	RhsChoice choice(Collection<RhsPart> parts, SourceElement origin);

	RhsSequence sequence(Collection<RhsPart> parts, SourceElement origin);

	RhsSequence empty(SourceElement origin);

	RhsUnordered unordered(Collection<RhsPart> parts, SourceElement origin);

	RhsOptional optional(RhsPart inner, SourceElement origin);

	RhsList list(RhsPart inner, RhsPart separator, boolean nonEmpty, SourceElement origin);

	Collection<Rule> addRule(String alias, Nonterminal left, RhsPart rhSide, Terminal prio);


	InputRef addInput(Nonterminal inputSymbol, boolean hasEoi, SourceElement origin);

	Prio addPrio(int prio, Collection<Terminal> symbols, SourceElement origin);

	Grammar create();
}
