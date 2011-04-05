/**
 * Copyright 2002-2010 Evgeny Gryaznov
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

import org.textway.lapg.lex.CharacterSet;
import org.textway.lapg.regex.RegexDefLexer.ErrorReporter;

import java.util.List;

/**
 * Gryaznov Evgeny, 4/5/11
 */
class RegexUtil {

	static RegexPart wrap(RegexPart part) {
		if (!(part instanceof RegexList) || ((RegexList) part).isInParentheses()) {
			part = new RegexList(part);
		}
		((RegexList)part).setInParentheses();
		return part;
	}

	static RegexPart createSequence(RegexPart left, RegexPart right) {
		if (!(left instanceof RegexList) || ((RegexList) left).isInParentheses()) {
			left = new RegexList(left);
		}
		((RegexList) left).addElement(right);
		return left;
	}

	static RegexPart createOr(RegexPart left, RegexPart right) {
		if (!(left instanceof RegexOr)) {
			left = new RegexOr(left);
		}
		((RegexOr) left).addVariant(right);
		return left;
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

	static RegexSet toSet(List<RegexPart> charset, ErrorReporter reporter, CharacterSet.Builder builder, boolean inverted) {
		builder.clear();
		for (RegexPart part : charset) {
			if (part instanceof RegexChar) {
				char c = ((RegexChar) part).getChar();
				builder.addSymbol(c);
			} else if (part instanceof RegexRange) {
				RegexRange range = (RegexRange) part;
				builder.addRange(range.getLeft(), range.getRight());
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

	static char unescape(char c) {
		switch (c) {
			case 'a':
				return 7;
			case 'b':
				return '\b';
			case 'f':
				return '\f';
			case 'n':
				return '\n';
			case 'r':
				return '\r';
			case 't':
				return '\t';
		}
		return c;
	}

	static char unescapeOct(String s) {
		throw new UnsupportedOperationException("not implemented");
	}

	static int unescapeHex(String s) {
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

	static void escape(StringBuilder sb, char c) {
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

			case '\\':
			case '/':
				sb.append('\\');
				sb.append(c);
				return;
			case 7:
				sb.append("\\a");
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
		sb.append("\\x");
		String sym = Integer.toString(c, 16);
		if (sym.length() < 4) {
			sb.append("0000".substring(sym.length()));
		}
		sb.append(sym);
	}
}
