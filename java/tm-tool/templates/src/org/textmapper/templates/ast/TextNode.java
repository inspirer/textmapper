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
package org.textmapper.templates.ast;

import org.textmapper.templates.api.EvaluationContext;
import org.textmapper.templates.api.IEvaluationStrategy;
import org.textmapper.templates.ast.TemplatesTree.TextSource;

public class TextNode extends Node {
	public TextNode(TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
	}

	public String getText() {
		return toString();
	}

	@Override
	protected void emit(StringBuilder sb, EvaluationContext context, IEvaluationStrategy env) {
		sb.append(getText());
	}

	@Override
	public void toJavascript(StringBuilder sb) {
		sb.append("'").append(escape(getText())).append("'");
	}

	private static void appendEscaped(StringBuilder sb, char c) {
		String sym = Integer.toString(c, 16);
		boolean isShort = c <= 0xff;
		sb.append(isShort ? "\\x" : "\\u");
		int len = isShort ? 2 : 4;
		if (sym.length() < len) {
			sb.append("0000".substring(sym.length() + (4 - len)));
		}
		sb.append(sym);
	}

	public static String escape(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
				case '"':
				case '\'':
				case '\\':
					sb.append('\\');
					sb.append(c);
					continue;
				case '\f':
					sb.append("\\f");
					continue;
				case '\n':
					sb.append("\\n");
					continue;
				case '\r':
					sb.append("\\r");
					continue;
				case '\t':
					sb.append("\\t");
					continue;
			}
			if (c >= 0x20 && c < 0x80) {
				sb.append(c);
				continue;
			}
			appendEscaped(sb, c);
		}
		return sb.toString();
	}
}
