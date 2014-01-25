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
package org.textmapper.tool.compiler;

import org.textmapper.tool.gen.TemplateStaticMethods;
import org.textmapper.tool.parser.TMLexer;
import org.textmapper.tool.parser.TMLexer.ErrorReporter;
import org.textmapper.tool.parser.TMLexer.LapgSymbol;
import org.textmapper.tool.parser.TMLexer.Lexems;
import org.textmapper.tool.parser.TMTree.TextSource;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * evgeny, 1/14/13
 */
public class TMTextUtil {

	public static String extractCopyright(TextSource source) {
		final boolean[] hasErrors = new boolean[]{false};
		ErrorReporter reporter = new ErrorReporter() {
			@Override
			public void error(String s, int line, int offset, int endoffset) {
				hasErrors[0] = true;
			}
		};

		try {
			TMLexer lexer = new TMLexer(source.getStream(), reporter);
			lexer.setSkipComments(false);
			List<String> headers = new LinkedList<String>();

			LapgSymbol sym = lexer.next();
			int lastline = 0;
			StringBuilder sb = new StringBuilder();
			while (sym.symbol == Lexems._skip_comment && source.columnForOffset(sym.offset) == 0) {
				String val = lexer.current().substring(1);
				if (val.endsWith("\n")) {
					val = val.substring(0, val.length() - (val.endsWith("\r\n") ? 2 : 1));
				}
				if (sym.line > lastline + 1 && sb.length() > 0) {
					headers.add(sb.toString());
					sb.setLength(0);
				}
				lastline = sym.line;
				if (!(sym.line == 1 && val.startsWith("!"))) {
					sb.append(val).append('\n');
				}
				sym = lexer.next();
			}
			if (hasErrors[0]) {
				return null;
			}
			if (sb.length() > 0) {
				headers.add(sb.toString());
			}
			for (String s : headers) {
				if (s.toLowerCase().contains("license")) {
					return new TemplateStaticMethods().shiftLeft(s);
				}
			}

		} catch (IOException e) {
			/* ignore */
		}

		return null;
	}

}
