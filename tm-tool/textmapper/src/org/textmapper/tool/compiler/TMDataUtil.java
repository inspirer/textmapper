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
import org.textmapper.lapg.api.rule.RhsSymbol;
import org.textmapper.tool.parser.ast.TmaCommand;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * evgeny, 1/15/13
 */
public class TMDataUtil {

	private static final String UD_CODE = "code";
	private static final String UD_ANNOTATIONS = "annotations";
	private static final String UD_IDENTIFIER = "id";
	private static final String UD_TRANSITIONMAP = "transitionMap";
	private static final String UD_CUSTOM_TYPE = "customType";
	private static final String UD_TYPE_HINT = "typeHint";
	private static final String UD_IMPLEMENTS = "implements";
	private static final String UD_LITERAL = "literal";
	private static final String UD_ROLE = "role";
	private static final String UD_RANGE_TYPE = "rangeType";
	private static final String UD_RANGE_FIELDS = "rangeFields";
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

	public static void putCode(UserDataHolder element, TmaCommand code) {
		element.putUserData(UD_CODE, code);
	}

	public static TmaCommand getCode(UserDataHolder element) {
		return (TmaCommand) lookupUserData(element, UD_CODE);
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
		return (Nonterminal) element.getUserData(UD_CUSTOM_TYPE);
	}

	public static void putTypeHint(Nonterminal element, TMTypeHint hint) {
		element.putUserData(UD_TYPE_HINT, hint);
	}

	public static TMTypeHint getTypeHint(Nonterminal element) {
		return (TMTypeHint) element.getUserData(UD_TYPE_HINT);
	}

	public static void putImplements(Nonterminal element, List<Nonterminal> interfaces) {
		element.putUserData(UD_IMPLEMENTS, interfaces);
	}

	public static List<Nonterminal> getImplements(Nonterminal element) {
		return (List<Nonterminal>) element.getUserData(UD_IMPLEMENTS);
	}

	public static void putTransition(LexerRule rule, TMStateTransitionSwitch transitionSwitch) {
		rule.putUserData(UD_TRANSITIONMAP, transitionSwitch);
	}

	public static TMStateTransitionSwitch getTransition(LexerRule rule) {
		return (TMStateTransitionSwitch) rule.getUserData(UD_TRANSITIONMAP);
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

	public static void putRole(RhsSymbol rhsSym, String role) {
		rhsSym.putUserData(UD_ROLE, role);
	}

	public static String getRole(RhsSymbol rhsSym) {
		return (String) rhsSym.getUserData(UD_ROLE);
	}

	public static void putRangeType(Rule rule, String type) {
		rule.putUserData(UD_RANGE_TYPE, type);
	}

	public static String getRangeType(Rule rule) {
		return (String) rule.getUserData(UD_RANGE_TYPE);
	}

	public static void putRangeFields(Grammar grammar, String type, Collection<? extends RangeTypeField> fields) {
		Map<String, Collection<? extends RangeTypeField>> map = (Map<String, Collection<?
				extends RangeTypeField>>) grammar.getUserData(UD_RANGE_FIELDS);
		if (map == null) {
			grammar.putUserData(UD_RANGE_FIELDS, (map = new HashMap<>()));
		}
		map.put(type, fields);
	}

	public static Collection<? extends RangeTypeField> getRangeFields(Grammar grammar, String type) {
		Map<String, Collection<? extends RangeTypeField>> map = (Map<String, Collection<?
				extends RangeTypeField>>) grammar.getUserData(UD_RANGE_FIELDS);
		return map == null ? null : map.get(type);
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
