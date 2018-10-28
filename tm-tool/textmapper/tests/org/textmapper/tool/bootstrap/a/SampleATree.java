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
package org.textmapper.tool.bootstrap.a;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.textmapper.tool.bootstrap.a.SampleALexer.ErrorReporter;
import org.textmapper.tool.bootstrap.a.SampleAParser.ParseException;
import org.textmapper.tool.bootstrap.a.ast.AstClassdef;
import org.textmapper.tool.bootstrap.a.ast.IAstClassdefNoEoi;

public class SampleATree<T> {

	private final TextSource source;
	private final T root;
	private final List<SampleAProblem> errors;

	public SampleATree(TextSource source, T root, List<SampleAProblem> errors) {
		this.source = source;
		this.root = root;
		this.errors = errors;
	}

	public TextSource getSource() {
		return source;
	}

	public T getRoot() {
		return root;
	}

	public List<SampleAProblem> getErrors() {
		return errors;
	}

	public boolean hasErrors() {
		return errors.size() > 0;
	}


	public static SampleATree<IAstClassdefNoEoi> parseClassdef_no_eoi(TextSource source) {
		final List<SampleAProblem> list = new ArrayList<>();
		ErrorReporter reporter = (message, line, offset, column, endline, endoffset, endcolumn) ->
				list.add(new SampleAProblem(KIND_ERROR, message, line, offset, column, endline, endoffset, endcolumn, null));

		try {
			SampleALexer lexer = new SampleALexer(source.getContents(), reporter);
			lexer.setLine(source.getInitialLine());

			SampleAParser parser = new SampleAParser(reporter);
			IAstClassdefNoEoi result = parser.parseClassdef_no_eoi(lexer);

			return new SampleATree<>(source, result, list);
		} catch (ParseException ex) {
			/* not parsed */
		} catch (IOException ex) {
			list.add(new SampleAProblem(KIND_FATAL, "I/O problem: " + ex.getMessage(), 0, 0, 0, 0, 0, 0, ex));
		}
		return new SampleATree<>(source, null, list);
	}

	public static SampleATree<AstClassdef> parseClassdef(TextSource source) {
		final List<SampleAProblem> list = new ArrayList<>();
		ErrorReporter reporter = (message, line, offset, column, endline, endoffset, endcolumn) ->
				list.add(new SampleAProblem(KIND_ERROR, message, line, offset, column, endline, endoffset, endcolumn, null));

		try {
			SampleALexer lexer = new SampleALexer(source.getContents(), reporter);
			lexer.setLine(source.getInitialLine());

			SampleAParser parser = new SampleAParser(reporter);
			AstClassdef result = parser.parseClassdef(lexer);

			return new SampleATree<>(source, result, list);
		} catch (ParseException ex) {
			/* not parsed */
		} catch (IOException ex) {
			list.add(new SampleAProblem(KIND_FATAL, "I/O problem: " + ex.getMessage(), 0, 0, 0, 0, 0, 0, ex));
		}
		return new SampleATree<>(source, null, list);
	}


	public static final int KIND_FATAL = 0;
	public static final int KIND_ERROR = 1;
	public static final int KIND_WARN = 2;

	public static final String PARSER_SOURCE = "parser";

	public static class SampleAProblem extends Exception {
		private static final long serialVersionUID = 1L;

		private final int kind;
		private final int line;
		private final int offset;
		private final int column;
		private final int endline;
		private final int endoffset;
		private final int endcolumn;

		public SampleAProblem(int kind, String message, int line, int offset, int column, int endline, int endoffset, int endcolumn, Throwable cause) {
			super(message, cause);
			this.kind = kind;
			this.line = line;
			this.offset = offset;
			this.column = column;
			this.endline = endline;
			this.endoffset = endoffset;
			this.endcolumn = endcolumn;
		}

		public int getKind() {
			return kind;
		}

		public int getLine() {
			return line;
		}

		public int getOffset() {
			return offset;
		}

		public int getColumn() {
			return column;
		}

		public int getEndline() {
			return endline;
		}

		public int getEndoffset() {
			return endoffset;
		}

		public int getEndcolumn() {
			return endcolumn;
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

		public int columnForOffset(int offset) {
			if (lineoffset == null) {
				lineoffset = getLineOffsets(contents);
			}
			int line = Arrays.binarySearch(lineoffset, offset);
			return offset >= 0 ? offset - lineoffset[line >= 0 ? line : -line - 2] : 0;
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
