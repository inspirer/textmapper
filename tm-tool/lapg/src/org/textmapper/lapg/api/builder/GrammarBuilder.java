/**
 * Copyright 2002-2014 Evgeny Gryaznov
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
import org.textmapper.lapg.api.ast.AstType;
import org.textmapper.lapg.api.regex.RegexPart;
import org.textmapper.lapg.api.rule.*;
import org.textmapper.lapg.api.rule.RhsSet.Operation;

import java.util.Collection;

public interface GrammarBuilder extends GrammarMapper {

	Terminal addTerminal(String name, AstType type, SourceElement origin);

	void makeSoft(Terminal terminal, Terminal softClass);

	Nonterminal addNonterminal(String name, SourceElement origin);

	Nonterminal addAnonymous(String nameHint, SourceElement origin);

	Nonterminal addShared(RhsPart part, String nameHint);

	Terminal getEoi();

	TemplateParameter addParameter(TemplateParameter.Type type,
								   String name, Object defaultValue, SourceElement origin);

	TemplateEnvironment getRootEnvironment();

	NamedPattern addPattern(String name, RegexPart regexp, SourceElement origin);

	LexerState addState(String name, SourceElement origin);

	LexerRule addLexerRule(int kind, Terminal sym, RegexPart regexp, Iterable<LexerState> states,
						   int priority, LexerRule classLexerRule, SourceElement origin);

	RhsArgument argument(TemplateParameter param, Object value, SourceElement origin);

	RhsSymbol symbol(Symbol sym, Collection<RhsArgument> args, SourceElement origin);

	RhsSymbol templateSymbol(TemplateParameter parameter, Collection<RhsArgument> args, SourceElement origin);

	RhsAssignment assignment(String name, RhsPart inner, boolean isAddition, SourceElement origin);

	RhsCast cast(Symbol asSymbol, Collection<RhsArgument> args, RhsPart inner, SourceElement origin);

	RhsChoice choice(Collection<RhsPart> parts, SourceElement origin);

	RhsPredicate predicate(RhsPredicate.Operation operation,
						   TemplateParameter param, Object value,
						   SourceElement origin);

	RhsPredicate compositePredicate(RhsPredicate.Operation operation,
									Collection<RhsPredicate> children, SourceElement origin);

	RhsSequence sequence(String name, Collection<RhsPart> parts, SourceElement origin);

	RhsSequence empty(SourceElement origin);

	RhsUnordered unordered(Collection<RhsPart> parts, SourceElement origin);

	RhsOptional optional(RhsPart inner, SourceElement origin);

	RhsList list(RhsSequence inner, RhsPart separator, boolean nonEmpty, SourceElement origin);

	RhsIgnored.ParenthesisPair parenthesisPair(Terminal opening, Terminal closing);

	RhsIgnored ignored(RhsPart inner, Collection<RhsIgnored.ParenthesisPair> parentheses, SourceElement origin);

	RhsSet set(Operation operation,
			   Symbol symbol, Collection<RhsArgument> args,
			   Collection<RhsSet> parts, SourceElement origin);

	void addRule(Nonterminal left, RhsPart rhSide, Terminal prio);


	InputRef addInput(Nonterminal inputSymbol, boolean hasEoi, SourceElement origin);

	Prio addPrio(int prio, Collection<Terminal> symbols, SourceElement origin);


	Grammar create();
}
