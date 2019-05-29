/**
 * Copyright 2002-2019 Evgeny Gryaznov
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
import org.textmapper.lapg.api.rule.RhsSequence;
import org.textmapper.lapg.api.rule.RhsSymbol;
import org.textmapper.tool.parser.ast.TmaCommand;

import java.util.*;

/**
 * evgeny, 1/15/13
 */
public class TMDataUtil {

	private static final String UD_CODE = "code";
	private static final String UD_CODE_TEMPLATE = "codeTemplate";
	private static final String UD_ANNOTATIONS = "annotations";
	private static final String UD_IDENTIFIER = "id";
	private static final String UD_CUSTOM_TYPE = "customType";
	private static final String UD_TYPE_HINT = "typeHint";
	private static final String UD_IMPLEMENTS = "implements";
	private static final String UD_EXCLUSIVE = "exclusive";
	private static final String UD_LITERAL = "literal";
	private static final String UD_RANGE_TYPE = "rangeType";
	private static final String UD_RANGE_TYPES = "rangeTypes";
	private static final String UD_RANGE_FIELDS = "rangeFields";
	private static final String UD_CATEGORIES = "categories";
	private static final String UD_CUSTOM_RANGES = "customRanges";
	private static final String UD_USER_REQUESTED_INPUT = "userRequested";

	private static Object lookupUserData(UserDataHolder element, String key) {
		while (element != null) {
			Object result = element.getUserData(key);
			if (result != null) return result;

			if (!(element instanceof DerivedSourceElement)) return null;
			SourceElement origin = ((DerivedSourceElement) element).getOrigin();
			if (!(origin instanceof UserDataHolder)) return null;
			element = (UserDataHolder) origin;
		}
		return null;
	}

	public static void putAnnotations(UserDataHolder element, Map<String, Object> annotations) {
		element.putUserData(UD_ANNOTATIONS, annotations);
	}

	public static Map<String, Object> getAnnotations(UserDataHolder element) {
		return (Map<String, Object>) lookupUserData(element, UD_ANNOTATIONS);
	}

	public static void putCodeTemplate(UserDataHolder element, TmaCommand code) {
		element.putUserData(UD_CODE_TEMPLATE, code);
	}

	public static TmaCommand getCodeTemplate(UserDataHolder element) {
		return (TmaCommand) lookupUserData(element, UD_CODE_TEMPLATE);
	}

	public static void putCode(UserDataHolder element, String code) {
		element.putUserData(UD_CODE, code);
	}

	public static String getCode(UserDataHolder rule) {
		return (String) rule.getUserData(UD_CODE);
	}

	public static void putId(Symbol element, String identifier) {
		element.putUserData(UD_IDENTIFIER, identifier);
	}

	public static String getId(Symbol element) {
		return (String) element.getUserData(UD_IDENTIFIER);
	}

	public static void putCustomType(Nonterminal element, Nonterminal type) {
		element.putUserData(UD_CUSTOM_TYPE, type);
	}

	public static Nonterminal getCustomType(Nonterminal element) {
		return (Nonterminal) lookupUserData(element, UD_CUSTOM_TYPE);
	}

	public static void putTypeHint(Nonterminal element, TMTypeHint hint) {
		element.putUserData(UD_TYPE_HINT, hint);
	}

	public static TMTypeHint getTypeHint(Nonterminal element) {
		return (TMTypeHint) lookupUserData(element, UD_TYPE_HINT);
	}

	public static void putImplements(Nonterminal element, List<Nonterminal> interfaces) {
		element.putUserData(UD_IMPLEMENTS, interfaces);
	}

	public static List<Nonterminal> getImplements(Nonterminal element) {
		return (List<Nonterminal>) element.getUserData(UD_IMPLEMENTS);
	}

	public static void makeExclusive(LexerState state) {
		state.putUserData(UD_EXCLUSIVE, Boolean.TRUE);
	}

	public static boolean isExclusive(LexerState state) {
		return state.getUserData(UD_EXCLUSIVE) == Boolean.TRUE;
	}

	public static RhsSymbol getRewrittenTo(RhsSymbol source) {
		return (RhsSymbol) source.getUserData(RhsSymbol.UD_REWRITTEN);
	}

	public static void putLiteral(RhsSymbol rhsSym, Object literal) {
		assert literal instanceof String ||
				literal instanceof Boolean ||
				literal instanceof Integer;
		rhsSym.putUserData(UD_LITERAL, literal);
	}

	public static Object getLiteral(RhsSymbol rhsSym) {
		return rhsSym.getUserData(UD_LITERAL);
	}

	public static void putRangeType(UserDataHolder udh, RangeType type) {
		udh.putUserData(UD_RANGE_TYPE, type);
	}

	public static RangeType getRangeType(UserDataHolder udh) {
		if (udh instanceof Rule) {
			udh = ((Rule) udh).getSource();
		}
		RangeType rangeType = (RangeType) lookupUserData(udh, UD_RANGE_TYPE);
		if (rangeType == null && udh instanceof RhsSequence && ((RhsSequence) udh).getContext() == null) {
			// For top-level sequences, we also check the nonterminal default type.
			return (RangeType) lookupUserData(((RhsSequence) udh).getLeft(), UD_RANGE_TYPE);
		}
		return rangeType;
	}

	public static void putRangeFields(Grammar grammar, String type, Collection<? extends RangeField> fields) {
		Map<String, Collection<? extends RangeField>> map = (Map<String, Collection<?
				extends RangeField>>) grammar.getUserData(UD_RANGE_FIELDS);
		if (map == null) {
			grammar.putUserData(UD_RANGE_FIELDS, (map = new HashMap<>()));
		}
		map.put(type, fields);
	}

	public static Collection<? extends RangeField> getRangeFields(Grammar grammar, String type) {
		Map<String, Collection<? extends RangeField>> map = (Map<String, Collection<?
				extends RangeField>>) grammar.getUserData(UD_RANGE_FIELDS);
		return map == null ? null : map.get(type);
	}

	public static void putCategory(Grammar grammar, String category, Collection<String> types) {
		Map<String, Collection<String>> map = (Map<String, Collection<String>>) grammar
				.getUserData(UD_CATEGORIES);
		if (map == null) {
			grammar.putUserData(UD_CATEGORIES, (map = new HashMap<>()));
		}
		map.put(category, types);
	}

	public static Collection<String> getCategoryTypes(Grammar grammar, String category) {
		Map<String, Collection<String>> map = (Map<String, Collection<String>>) grammar
				.getUserData(UD_CATEGORIES);
		return map == null ? null : map.get(category);
	}

	public static void putTypes(Grammar grammar, Collection<String> rangeTypes) {
		grammar.putUserData(UD_RANGE_TYPES, rangeTypes);
	}

	public static Collection<String> getTypes(Grammar grammar) {
		return (Collection<String>) grammar.getUserData(UD_RANGE_TYPES);
	}

	public static void putCustomRange(Rule rule, CustomRange range) {
		if (range == null) return;

		Collection<CustomRange> list = (Collection<CustomRange>) rule.getUserData(UD_CUSTOM_RANGES);
		if (list == null) {
			rule.putUserData(UD_CUSTOM_RANGES, (list = new ArrayList<>()));
		}
		list.add(range);
	}

	public static Collection<CustomRange> getCustomRanges(Rule rule) {
		return (Collection<CustomRange>) rule.getUserData(UD_CUSTOM_RANGES);
	}

	public static Collection<String> getCategoryList(Grammar grammar) {
		Map<String, Collection<String>> map = (Map<String, Collection<String>>) grammar
				.getUserData(UD_CATEGORIES);
		return map == null ? Collections.emptyList() : new ArrayList<>(map.keySet());
	}

	public static void setUserRequested(InputRef input) {
		input.putUserData(UD_USER_REQUESTED_INPUT, Boolean.TRUE);
	}

	public static boolean isUserRequested(InputRef input) {
		return input.getUserData(UD_USER_REQUESTED_INPUT) == Boolean.TRUE;
	}

	public static boolean hasProperty(UserDataHolder o, String name) {
		Map<String, Object> annotations = TMDataUtil.getAnnotations(o);
		if (annotations == null) {
			return false;
		}
		Object o1 = annotations.get(name);
		return o1 instanceof Boolean ? (Boolean) o1 : false;
	}
}
