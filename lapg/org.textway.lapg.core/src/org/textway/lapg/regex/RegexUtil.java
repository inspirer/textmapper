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
package org.textway.lapg.regex;

import org.textway.lapg.api.regex.CharacterSet;
import org.textway.lapg.common.CharacterSetImpl.Builder;
import org.textway.lapg.regex.RegexDefLexer.ErrorReporter;
import org.textway.lapg.regex.RegexDefTree.TextSource;
import org.textway.lapg.unicode.UnicodeData;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gryaznov Evgeny, 4/5/11
 */
public class RegexUtil {

	private static final Pattern QUANTIFIER = Pattern.compile("(\\d+)(,(\\d+)?)?");
	private static final Pattern IDENTIFIER = Pattern.compile("[a-zA-Z_][a-zA-Z_0-9]*|'([^\\n\\\\']|\\\\.)*'");

	static RegexPart wrap(RegexPart part) {
		if (!(part instanceof RegexList) || ((RegexList) part).isInParentheses()) {
			part = new RegexList(part);
		}
		((RegexList) part).setInParentheses();
		return part;
	}

	static RegexPart createSequence(RegexPart left, RegexPart right) {
		if (!(left instanceof RegexList) || ((RegexList) left).isInParentheses()) {
			left = new RegexList(left);
		}
		if (right instanceof RegexList && !(((RegexList) right).isInParentheses())) {
			for (org.textway.lapg.api.regex.RegexPart item : ((RegexList) right).getElements()) {
				((RegexList) left).addElement((RegexPart) item);
			}
		} else {
			((RegexList) left).addElement(right);
		}
		return left;
	}

	static RegexPart emptyIfNull(RegexPart part, TextSource source, int offset) {
		return part != null
				? part
				: new RegexEmpty(source, offset);
	}

	static RegexPart createOr(RegexPart left, RegexPart right, TextSource source, int offset) {
		if (!(left instanceof RegexOr)) {
			left = new RegexOr(left);
		}
		((RegexOr) left).addVariant(emptyIfNull(right, source, offset));
		return left;
	}

	static void addSetSymbol(List<RegexPart> charset, RegexPart right, ErrorReporter reporter) {
		if (right instanceof RegexOr) {
			for (org.textway.lapg.api.regex.RegexPart regexPart : ((RegexOr) right).getVariants()) {
				addSetSymbol(charset, (RegexPart) regexPart, reporter);
			}
		} else {
			charset.add(right);
		}
	}

	static void applyRange(List<RegexPart> charset, RegexChar right, ErrorReporter reporter) {
		RegexPart last = charset.get(charset.size() - 1);

		if (last instanceof RegexChar && isRangeChar(((RegexChar) last).getChar())) {
			if (isRangeChar(right.getChar())) {
				charset.remove(charset.size() - 1);
				charset.add(new RegexRange(((RegexChar) last).getChar(), right.getChar(), right.getInput(), last.getOffset(), right.getEndOffset()));
				return;
			} else {
				reporter.error(right.getOffset(), right.getEndOffset(), right.getInput().lineForOffset(right.getOffset()), "invalid range in character class (after dash): `" + right.toString() + "', escape `-'");
			}
		} else {
			reporter.error(last.getOffset(), right.getEndOffset(), right.getInput().lineForOffset(last.getOffset()), "invalid range in character class (before dash): `" + last.toString() + "', escape `-'");
		}

		charset.add(new RegexChar('-', right.getInput(), right.getOffset() - 1, right.getOffset()));
		charset.add(right);
	}

	static RegexSet toSet(List<RegexPart> charset, ErrorReporter reporter, Builder builder, boolean inverted) {
		builder.clear();
		for (RegexPart part : charset) {
			if (part instanceof RegexChar) {
				char c = ((RegexChar) part).getChar();
				builder.addSymbol(c);
			} else if (part instanceof RegexRange) {
				RegexRange range = (RegexRange) part;
				builder.addRange(range.getLeft(), range.getRight());
			} else if (part instanceof RegexCharClass) {
				for (int[] range : ((RegexCharClass) part).getSet()) {
					builder.addRange(range[0], range[1]);
				}
			} else {
				throw new IllegalStateException("unknown part: " + part.getClass());
			}
		}
		return new RegexSet(builder.create(inverted), charset, charset.get(0).getInput(), charset.get(0).getOffset(), charset.get(charset.size() - 1).getEndOffset());
	}

	private static boolean isRangeChar(char c) {
		switch (c) {
			case '.':
			case '-':
			case '^':
			case '(':
			case '|':
			case ')':
			case '{':
			case '}':
			case '[':
			case ']':
			case '*':
			case '+':
			case '?':
			case '/':
			case '\\':
				return false;
		}
		return c >= 0x20;
	}

	static char unescapeOct(String s) {
		assert s.length() == 3;
		int result = 0;
		for (int i = 0; i < 3; i++) {
			result <<= 3;
			int c = s.codePointAt(i);
			if (c >= '0' && c <= '7') {
				result |= c - '0';
			} else {
				throw new NumberFormatException();
			}
		}
		return (char) result;
	}

	public static char unescapeHex(String s) {
		int result = 0;
		for (int i = 0; i < s.length(); i++) {
			result <<= 4;
			int c = s.codePointAt(i);
			if (c >= 'a' && c <= 'f') {
				result |= 10 + c - 'a';
			} else if (c >= 'A' && c <= 'F') {
				result |= 10 + c - 'A';
			} else if (c >= '0' && c <= '9') {
				result |= c - '0';
			} else {
				throw new NumberFormatException();
			}
		}
		return (char) result;
	}

	static void escape(StringBuilder sb, char c, boolean inSet) {
		switch (c) {
			case '(':
			case '|':
			case ')':
			case '{':
			case '}':
			case '*':
			case '+':
			case '?':
				if (!inSet) {
					sb.append('\\');
				}
				sb.append(c);
				return;
			case '.':
			case '-':
			case '^':
			case '[':
			case ']':
			case '\\':
			case '/':
				sb.append('\\');
				sb.append(c);
				return;
			case 7:
				sb.append("\\a");
				return;
			case 0xb:
				sb.append("\\v");
				return;
			case '\b':
				sb.append("\\b");
				return;
			case '\f':
				sb.append("\\f");
				return;
			case '\n':
				sb.append("\\n");
				return;
			case '\r':
				sb.append("\\r");
				return;
			case '\t':
				sb.append("\\t");
				return;
		}
		if (c >= 0x20 && c < 0x80) {
			sb.append(c);
			return;
		}
		String sym = Integer.toString(c, 16);
		boolean isShort = sym.length() <= 2;
		sb.append(isShort ? "\\x" : "\\u");
		int len = isShort ? 2 : 4;
		if (sym.length() < len) {
			sb.append("0000".substring(sym.length() + (4 - len)));
		}
		sb.append(sym);
	}

	static RegexPart createQuantifier(RegexPart sym, TextSource source, int quantifierStart, int quantifierEnd, ErrorReporter reporter) {
		String innerText = source.getText(quantifierStart + 1, quantifierEnd - 1);
		Matcher matcher = QUANTIFIER.matcher(innerText);
		if (matcher.matches()) {
			int min = Integer.parseInt(matcher.group(1));
			int max = min;
			if (matcher.group(2) != null) {
				String second = matcher.group(3);
				max = second != null ? Integer.parseInt(second) : -1;
			}
			return new RegexQuantifier(sym, min, max, sym.getInput(), sym.getOffset(), quantifierEnd);
		}

		reporter.error(quantifierStart, quantifierEnd, source.lineForOffset(quantifierStart),
				"quantifier range is expected instead of `" + innerText + "'");
		return sym;
	}

	static void checkExpand(RegexExpand expand, ErrorReporter reporter) {
		String innerText = expand.getInput().getText(expand.getOffset() + 1, expand.getEndOffset() - 1);
		if (!IDENTIFIER.matcher(innerText).matches()) {
			reporter.error(expand.getOffset(), expand.getEndOffset(), expand.getInput().lineForOffset(expand.getOffset()),
					"an expansion identifier is expected instead of `" + innerText + "'");
		}
	}

	static CharacterSet getClassSet(String cl, Builder builder) {
		builder.clear();
		if (cl.length() == 1) {
			char c = cl.charAt(0);
			if (c == 'w' || c == 'W') {
				builder.addRange('0', '9');
				builder.addRange('a', 'z');
				builder.addRange('A', 'Z');
				builder.addSymbol('_');
				return builder.create(c == 'W');
			} else if (c == 's' || c == 'S') {
				builder.addSymbol('\n');
				builder.addSymbol('\r');
				builder.addSymbol('\f');
				builder.addSymbol('\t');
				builder.addSymbol(0x0b);   // \v (Vertical Tab)
				builder.addSymbol(' ');
				return builder.create(c == 'S');
			} else if (c == 'd' || c == 'D') {
				builder.addRange('0', '9');
				return builder.create(c == 'D');
			}
		}
		Byte category = UnicodeData.categories.get(cl);
		if (category != null) {
			CharacterSet res = UnicodeData.getCategory(category);
			if (res != null) {
				return res;
			}
		}
		throw new UnsupportedOperationException("class: " + cl);
	}
}
