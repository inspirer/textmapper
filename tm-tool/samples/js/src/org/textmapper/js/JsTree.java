package org.textmapper.js;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.textmapper.js.JsLexer.ErrorReporter;
import org.textmapper.js.JsParser.ParseException;

public class JsTree<T> {

	private final TextSource source;
	private final T root;
	private final List<JsProblem> errors;

	public JsTree(TextSource source, T root, List<JsProblem> errors) {
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

	public List<JsProblem> getErrors() {
		return errors;
	}

	public boolean hasErrors() {
		return errors.size() > 0;
	}


	public static JsTree<Object> parse(TextSource source) {
		final List<JsProblem> list = new ArrayList<>();
		ErrorReporter reporter = (message, line, offset, endoffset) ->
				list.add(new JsProblem(KIND_ERROR, message, line, offset, endoffset, null));

		try {
			JsLexer lexer = new JsLexer(source.getStream(), reporter);
			lexer.setLine(source.getInitialLine());

			JsParser parser = new JsParser(reporter);
			Object result = parser.parse(lexer);

			return new JsTree<>(source, result, list);
		} catch (ParseException ex) {
			/* not parsed */
		} catch (IOException ex) {
			list.add(new JsProblem(KIND_FATAL, "I/O problem: " + ex.getMessage(), 0, 0, 0, ex));
		}
		return new JsTree<>(source, null, list);
	}


	public static final int KIND_FATAL = 0;
	public static final int KIND_ERROR = 1;
	public static final int KIND_WARN = 2;

	public static final String PARSER_SOURCE = "parser";

	public static class JsProblem extends Exception {
		private static final long serialVersionUID = 1L;

		private final int kind;
		private final int line;
		private final int offset;
		private final int endoffset;

		public JsProblem(int kind, String message, int line, int offset, int endoffset, Throwable cause) {
			super(message, cause);
			this.kind = kind;
			this.line = line;
			this.offset = offset;
			this.endoffset = endoffset;
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

		public int getEndoffset() {
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
