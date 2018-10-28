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
package org.textmapper.lapg.regex;

import org.textmapper.lapg.api.regex.CharacterSet;
import org.textmapper.lapg.api.regex.RegexPart;
import org.textmapper.lapg.common.CharacterSetImpl;
import org.textmapper.lapg.common.CharacterSetImpl.Builder;
import org.textmapper.lapg.regex.RegexDefLexer.ErrorReporter;
import org.textmapper.lapg.regex.RegexDefTree.TextSource;
import org.textmapper.lapg.unicode.UnicodeData;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gryaznov Evgeny, 4/5/11
 */
class RegexUtil {

	private static final Pattern QUANTIFIER = Pattern.compile("(\\d+)(,(\\d+)?)?");
	private static final Pattern IDENTIFIER = Pattern.compile(
			"[a-zA-Z_]([a-zA-Z_\\-0-9]*[a-zA-Z_0-9])?|'([^\\n\\\\']|\\\\.)*'");

	static RegexAstPart wrap(RegexAstPart part) {
		if (!(part instanceof RegexAstList) || ((RegexAstList) part).isParenthesized()) {
			part = new RegexAstList(part);
		}
		((RegexAstList) part).setInParentheses();
		return part;
	}

	static RegexAstPart createSequence(RegexAstPart left, RegexAstPart right) {
		if (!(left instanceof RegexAstList) || ((RegexAstList) left).isParenthesized()) {
			left = new RegexAstList(left);
		}
		if (right instanceof RegexAstList && !(((RegexAstList) right).isParenthesized())) {
			for (RegexPart item : ((RegexAstList) right).getElements()) {
				((RegexAstList) left).addElement((RegexAstPart) item);
			}
		} else {
			((RegexAstList) left).addElement(right);
		}
		return left;
	}

	static RegexAstPart emptyIfNull(RegexAstPart part, TextSource source, int offset) {
		return part != null
				? part
				: new RegexAstEmpty(source, offset);
	}

	static RegexAstPart createOr(RegexAstPart left, RegexAstPart right, TextSource source, int offset) {
		if (!(left instanceof RegexAstOr)) {
			left = new RegexAstOr(left);
		}
		((RegexAstOr) left).addVariant(emptyIfNull(right, source, offset));
		return left;
	}

	static void addSetSymbol(List<RegexAstPart> charset, RegexAstPart right, ErrorReporter reporter) {
		if (right instanceof RegexAstOr) {
			for (RegexPart regexPart : ((RegexAstOr) right).getVariants()) {
				addSetSymbol(charset, (RegexAstPart) regexPart, reporter);
			}
		} else {
			charset.add(right);
		}
	}

	static void applyRange(List<RegexAstPart> charset, RegexAstChar right, ErrorReporter reporter) {
		RegexAstPart last = charset.get(charset.size() - 1);

		if (last instanceof RegexAstChar && isRangeChar(((RegexAstChar) last).getChar())) {
			if (isRangeChar(right.getChar())) {
				charset.remove(charset.size() - 1);
				charset.add(new RegexAstRange(((RegexAstChar) last).getChar(), right.getChar(),
						right.getInput(), last.getOffset(), right.getEndOffset()));
				return;
			} else {
				reporter.error("invalid range in character class (after dash): `" + right.toString() + "', escape `-'",
						right.getOffset(), right.getEndOffset());
			}
		} else {
			reporter.error("invalid range in character class (before dash): `" + last.toString() + "', escape `-'",
					last.getOffset(), right.getEndOffset());
		}

		charset.add(new RegexAstChar('-', right.getInput(), right.getOffset() - 1, right.getOffset()));
		charset.add(right);
	}

	static RegexAstSet toSet(List<RegexAstPart> charset, ErrorReporter reporter, Builder builder, boolean inverted) {
		builder.clear();
		for (RegexAstPart part : charset) {
			if (part instanceof RegexAstChar) {
				int c = ((RegexAstChar) part).getChar();
				builder.addSymbol(c);
			} else if (part instanceof RegexAstRange) {
				RegexAstRange range = (RegexAstRange) part;
				builder.addRange(range.getLeft(), range.getRight());
			} else if (part instanceof RegexAstCharClass) {
				for (int[] range : ((RegexAstCharClass) part).getSet()) {
					builder.addRange(range[0], range[1]);
				}
			} else {
				throw new IllegalStateException("unknown part: " + part.getClass());
			}
		}
		return new RegexAstSet(builder.create(inverted), charset, charset.get(0).getInput(), charset.get(0).getOffset(), charset.get(charset.size() - 1).getEndOffset());
	}

	private static boolean isRangeChar(int c) {
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

	static int unescapeOct(String s) {
		if (s.length() != 3) throw new IllegalArgumentException();

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

	public static int unescapeHex(String s) {
		if (s.length() > 8 || s.isEmpty()) {
			throw new IllegalArgumentException();
		}
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
		return result;
	}

	static void escape(StringBuilder sb, int c, boolean inSet) {
		if (!Character.isValidCodePoint(c)) {
			throw new IllegalArgumentException();
		}
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
				sb.append((char) c);
				return;
			case '.':
			case '-':
			case '^':
			case '[':
			case ']':
			case '\\':
			case '/':
				sb.append('\\');
				sb.append((char) c);
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
			sb.append((char) c);
			return;
		}
		String sym = Integer.toString(c, 16);
		boolean isShort = sym.length() <= 2;
		boolean isLong = sym.length() > 4;
		sb.append(isShort ? "\\x" : isLong ? "\\U" : "\\u");
		int len = isShort ? 2 : isLong ? 8 : 4;
		if (sym.length() < len) {
			sb.append("00000000".substring(sym.length() + (8 - len)));
		}
		sb.append(sym);
	}

	static RegexAstPart createQuantifier(RegexAstPart sym, TextSource source, int quantifierStart, int quantifierEnd, ErrorReporter reporter) {
		String innerText = source.getText(quantifierStart + 1, quantifierEnd - 1);
		Matcher matcher = QUANTIFIER.matcher(innerText);
		if (matcher.matches()) {
			int min = Integer.parseInt(matcher.group(1));
			int max = min;
			if (matcher.group(2) != null) {
				String second = matcher.group(3);
				max = second != null ? Integer.parseInt(second) : -1;
			}
			return new RegexAstQuantifier(sym, min, max, sym.getInput(), sym.getOffset(), quantifierEnd);
		}

		reporter.error("quantifier range is expected instead of `" + innerText + "'",
				quantifierStart, quantifierEnd);
		return sym;
	}

	static void checkExpand(RegexAstExpand expand, ErrorReporter reporter) {
		String innerText = expand.getInput().getText(expand.getOffset() + 1, expand.getEndOffset() - 1);
		if (!IDENTIFIER.matcher(innerText).matches()) {
			reporter.error("an expansion identifier is expected instead of `" + innerText + "'",
					expand.getOffset(), expand.getEndOffset());
		}
	}

	static CharacterSet getClassSet(String cl, Builder builder, ErrorReporter reporter, int offset, int endoffset) {
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
		CharacterSet res = UnicodeData.getInstance().getCharacterSet(cl);
		if (res != null) {
			return res;
		}
		reporter.error("unsupported character class: " + cl, offset, endoffset);
		return new CharacterSetImpl();
	}
}
