/**
 * Copyright 2002-2018 Evgeny Gryaznov
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
package org.textmapper.lapg.builder;

import org.textmapper.lapg.api.Name;
import org.textmapper.lapg.api.NameParseException;
import org.textmapper.lapg.common.FormatUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class LiName implements Name {
	private static final Pattern IDENTIFIER = Pattern.compile(
			"[a-zA-Z_]([a-zA-Z_\\-0-9]*[a-zA-Z_0-9])?");

	private final String[] words;
	private final String[] aliases;
	private final Name qualifier;

	private String camel1;
	private String camel2;
	private String snake1;
	private String snake2;
	private String allUpper;

	private LiName(String[] words, String[] aliases, Name qualifier) {
		this.words = words;
		this.aliases = aliases;
		this.qualifier = qualifier;
		if (aliases == null || aliases.length == 0) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public String[] uniqueIds() {
		LinkedHashSet<String> ids = new LinkedHashSet<>();
		for (String alias : aliases) {
			if (alias.startsWith("'")) continue;
			String[] words;
			try {
				words = parseWords(alias);
			} catch (NameParseException e) {
				throw new IllegalStateException();
			}
			ids.add(Arrays.stream(words).collect(Collectors.joining()));
		}
		ids.addAll(Arrays.asList(aliases));
		return ids.toArray(new String[ids.size()]);
	}

	@Override
	public String text() {
		if (qualifier != null) {
			return qualifier.text() + "/" + aliases[0];
		}
		return aliases[0];
	}

	@Override
	public boolean isReference(String referenceText) {
		// We allow at most two aliases in grammars, so no need to optimize here.
		for (String alias : aliases) {
			if (alias.equals(referenceText)) {
				return true;
			}
		}
		return false;
	}

	private static String toFirstUpper(String s) {
		if (s.length() > 0) {
			return Character.toUpperCase(s.charAt(0)) + s.substring(1);
		}
		return s;
	}

	@Override
	public String camelCase(boolean firstUpper) {
		String result = firstUpper ? camel1 : camel2;
		if (result != null) return result;

		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String word : words) {
			sb.append(!first || firstUpper ? toFirstUpper(word) : word);
			if (first && !word.startsWith("_")) {
				first = false;
			}
		}
		result = sb.toString();
		if (firstUpper) {
			camel1 = result;
		} else {
			camel2 = result;
		}
		return result;
	}

	@Override
	public String snakeCase(boolean allUpper) {
		String result = allUpper ? snake1 : snake2;
		if (result != null) return result;

		StringBuilder sb = new StringBuilder();
		for (String word : words) {
			if (sb.length() > 0 && (sb.charAt(sb.length() - 1) != '_' && !word.startsWith("_"))) {
				sb.append('_');
			}
			sb.append(allUpper ? word.toUpperCase() : word);
		}
		result = sb.toString();
		if (allUpper) {
			snake1 = result;
		} else {
			snake2 = result;
		}
		return result;
	}

	@Override
	public String allUpper() {
		if (allUpper != null) return allUpper;

		StringBuilder sb = new StringBuilder();
		for (String word : words) {
			sb.append(word.toUpperCase());
		}
		allUpper = sb.toString();
		return allUpper;
	}

	@Override
	public Name qualifier() {
		return qualifier;
	}

	@Override
	public Name subName(Name nested) {
		LiName unqualified = (LiName) nested;
		if (unqualified.qualifier != null) {
			throw new IllegalArgumentException("already qualified");
		}
		return new LiName(unqualified.words, unqualified.aliases, this);
	}

	private static boolean isWordBoundary(String s, int i) {
		if (i >= s.length()) return true;

		char c = s.charAt(i);
		if (c == '_' || c == '-') return true;
		if (i == 0) return false;

		char prev = s.charAt(i - 1);
		if (prev == '_' || prev == '-') {
			if (Character.isUpperCase(c) || Character.isLowerCase(c)) return false;
		}

		if (Character.isUpperCase(c)) {
			// a sequence of uppercase characters
			if (Character.isUpperCase(prev) &&
					(i + 1 == s.length() || !Character.isLowerCase(s.charAt(i + 1)))) {
				// do not break
				return false;
			}
			return true;
		}
		return false;
	}

	private static String[] parseWords(String identifier) {
		if (!IDENTIFIER.matcher(identifier).matches()) {
			throw new NameParseException("malformed identifier: " + identifier);
		}
		List<String> result = new ArrayList<>();
		String suffix = null;
		if (identifier.indexOf('-') == -1) {
			int offset = 0;
			while (offset < identifier.length() && identifier.charAt(offset) == '_') {
				offset++;
			}
			if (offset > 0) {
				result.add(identifier.substring(0, offset));
			}
			int endoffset = identifier.length();
			while (endoffset > offset && identifier.charAt(endoffset - 1) == '_') {
				endoffset--;
			}
			if (endoffset < identifier.length()) {
				suffix = identifier.substring(endoffset);
			}
			identifier = identifier.substring(offset, endoffset);
		}

		if (identifier.indexOf('-') >= 0) {
			if (!identifier.equals(identifier.toLowerCase())) {
				throw new NameParseException(
						"dash-separated identifiers must be in lowercase: " + identifier);
			}
			if (identifier.indexOf('_') >= 0) {
				throw new NameParseException(
						"dash-separated identifiers cannot contain underscores: " + identifier);
			}
		} else if (identifier.lastIndexOf('_') >= 0) {
			if (!identifier.equals(identifier.toLowerCase()) &&
					!identifier.equals(identifier.toUpperCase())) {
				throw new NameParseException("underscore-separated identifiers must be either in" +
						" lowercase or in uppercase: " + identifier);
			}
		}

		int offset = 0;
		for (int i = offset; i <= identifier.length(); i++) {
			if (!isWordBoundary(identifier, i)) continue;

			if (offset == i) {
				throw new NameParseException("malformed identifier: " + identifier);
			}
			result.add(identifier.substring(offset, i).toLowerCase());
			offset = i;

			char c = i < identifier.length() ? identifier.charAt(i) : 0;
			if (c == '_' || c == '-') offset++;
		}
		if (suffix != null) {
			result.add(suffix);
		}
		String[] words = result.toArray(new String[result.size()]);
		if (words.length == 0) {
			throw new NameParseException("malformed identifier: " + identifier);
		}
		return words;
	}

	static Name create(String... aliases) {
		if (aliases.length == 0) {
			throw new IllegalArgumentException("no aliases provided");
		}
		String[] words = null;
		for (String id : aliases) {
			if (id.startsWith("'")) continue;
			if (words == null) {
				words = LiName.parseWords(id);
			} else {
				LiName.parseWords(id);
			}
		}
		if (words == null) {
			aliases = Arrays.copyOf(aliases, aliases.length + 1);
			aliases[aliases.length - 1] = toIdentifier(aliases[0]);
			words = LiName.parseWords(aliases[aliases.length - 1]);
		}
		return new LiName(words, aliases, null);
	}

	private static String toIdentifier(String s) {
		if (s.startsWith("\'") && s.endsWith("\'") && s.length() > 2) {
			s = s.substring(1, s.length() - 1);
			if (FormatUtil.isIdentifier(s)) {
				return (s.length() == 1 ? "char_" : "kw_") + s.toLowerCase();
			}
		}
		return FormatUtil.toIdentifier(s);
	}

	static Name raw(String word) {
		return new LiName(new String[]{word.toLowerCase()}, new String[]{word}, null);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LiName liName = (LiName) o;

		if (!Arrays.equals(aliases, liName.aliases)) return false;
		return qualifier != null ? qualifier.equals(liName.qualifier) : liName.qualifier == null;
	}

	@Override
	public int hashCode() {
		int result = Arrays.hashCode(aliases);
		result = 31 * result + (qualifier != null ? qualifier.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return text();
	}
}
