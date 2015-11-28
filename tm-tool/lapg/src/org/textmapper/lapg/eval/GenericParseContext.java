/**
 * Copyright 2002-2015 Evgeny Gryaznov
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
package org.textmapper.lapg.eval;

import org.textmapper.lapg.api.Grammar;
import org.textmapper.lapg.api.LexerData;
import org.textmapper.lapg.api.ParserData;
import org.textmapper.lapg.eval.GenericLexer.ErrorReporter;
import org.textmapper.lapg.eval.GenericParser.ParseException;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Gryaznov Evgeny, 3/17/11
 */
public class GenericParseContext {

	private final Grammar grammar;
	private final ParserData parserTables;
	private final LexerData lexerTables;

	public GenericParseContext(Grammar grammar, ParserData parserTables, LexerData lexerTables) {
		this.grammar = grammar;
		this.parserTables = parserTables;
		this.lexerTables = lexerTables;
	}

	public Result parse(CharSequence text, int inputIndex) {
		return parse(new TextSource("input", text, 1), inputIndex);
	}

	public Result parse(TextSource source, int inputIndex) {
		final List<ParseProblem> list = new ArrayList<>();
		ErrorReporter reporter = (s, line, offset, endoffset) ->
				list.add(new ParseProblem(KIND_ERROR, offset, endoffset, s, null));

		try {
			GenericLexer lexer = createLexer(source, reporter);
			lexer.setLine(source.getInitialLine());

			GenericParser parser = createParser(source, reporter);
			parser.source = source;
			Object result = parser.parse(lexer, inputIndex, parserTables.getFinalStates()[inputIndex], !grammar.getInput()[inputIndex].hasEoi());

			return new Result(source, result, list);
		} catch (ParseException ex) {
			/* not parsed */
		} catch (IOException ex) {
			list.add(new ParseProblem(KIND_FATAL, 0, 0, "I/O problem: " + ex.getMessage(), ex));
		}
		return new Result(source, null, list);
	}

	private GenericParser createParser(TextSource source, ErrorReporter reporter) {
		return new GenericParser(reporter, parserTables, grammar, false);
	}

	protected GenericLexer createLexer(TextSource source, ErrorReporter reporter) throws IOException {
		return new GenericLexer(source.getStream(), reporter, lexerTables, grammar);
	}

	public static class Result {
		private final TextSource source;
		private final Object root;
		private final List<ParseProblem> errors;

		public Result(TextSource source, Object root, List<ParseProblem> errors) {
			this.source = source;
			this.root = root;
			this.errors = errors;
		}

		public TextSource getSource() {
			return source;
		}

		public Object getRoot() {
			return root;
		}

		public List<ParseProblem> getErrors() {
			return errors;
		}
	}

	public static final int KIND_FATAL = 0;
	public static final int KIND_ERROR = 1;
	public static final int KIND_WARN = 2;

	public static final String PARSER_SOURCE = "parser";

	public static class ParseProblem extends Exception {
		private static final long serialVersionUID = 1L;

		private final int kind;
		private final int offset;
		private final int endoffset;

		public ParseProblem(int kind, int offset, int endoffset, String message, Throwable cause) {
			super(message, cause);
			this.kind = kind;
			this.offset = offset;
			this.endoffset = endoffset;
		}

		public int getKind() {
			return kind;
		}

		public int getOffset() {
			return offset;
		}

		public int getEndOffset() {
			return endoffset;
		}

		public String getSource() {
			return PARSER_SOURCE;
		}
	}

	public static class TextSource {

		private final String file;
		private final int initialLine;
		private final CharSequence contents;
		private int[] lineoffset;

		public TextSource(String file, CharSequence contents, int initialLine) {
			this.file = file;
			this.initialLine = initialLine;
			this.contents = contents;
		}

		public String getFile() {
			return file;
		}

		public int getInitialLine() {
			return initialLine;
		}

		public Reader getStream() {
			if (contents instanceof String) {
				return new StringReader((String) contents);
			} else {
				return new CharArrayReader(contents.toString().toCharArray());
			}
		}

		public String getLocation(int offset) {
			return file + "," + lineForOffset(offset);
		}

		public String getText(int start, int end) {
			if (start < 0 || start > end || end > contents.length()) {
				return "";
			}
			return contents.subSequence(start, end).toString();
		}

		public int lineForOffset(int offset) {
			if (lineoffset == null) {
				lineoffset = getLineOffsets(contents);
			}
			int line = Arrays.binarySearch(lineoffset, offset);
			return initialLine + (line >= 0 ? line : -line - 2);
		}

		public CharSequence getContents() {
			return contents;
		}
	}

	private static int[] getLineOffsets(CharSequence contents) {
		int size = 1;
		int len = contents.length();
		for (int i = 0; i < len; i++) {
			if (contents.charAt(i) == '\n') {
				size++;
			} else if (contents.charAt(i) == '\r') {
				if (i + 1 < len && contents.charAt(i + 1) == '\n') {
					i++;
				}
				size++;
			}
		}
		int[] result = new int[size];
		result[0] = 0;
		int e = 1;
		for (int i = 0; i < len; i++) {
			if (contents.charAt(i) == '\n') {
				result[e++] = i + 1;
			} else if (contents.charAt(i) == '\r') {
				if (i + 1 < len && contents.charAt(i + 1) == '\n') {
					i++;
				}
				result[e++] = i + 1;
			}
		}
		if (e != size) {
			throw new IllegalStateException();
		}
		return result;
	}
}
