package net.sf.lapg.templates.model.xml;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.text.MessageFormat;
import net.sf.lapg.templates.model.xml.XmlLexer.ErrorReporter;
import net.sf.lapg.templates.model.xml.XmlParser.ParseException;

public class XmlTree<T> {

	private final TextSource source;
	private final T root;
	private final List<XmlProblem> errors;

	public XmlTree(TextSource source, T root, List<XmlProblem> errors) {
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

	public List<XmlProblem> getErrors() {
		return errors;
	}

	public boolean hasErrors() {
		return errors.size() > 0;
	}


	public static XmlTree<XmlNode> parse(TextSource source) {
		final List<XmlProblem> list = new ArrayList<XmlProblem>();
		ErrorReporter reporter = new ErrorReporter() {
			public void error(int start, int end, int line, String s) {
				list.add(new XmlProblem(KIND_ERROR, start, end, s, null));
			}
		};

		try {
			XmlLexer lexer = new XmlLexer(source.getStream(), reporter);
			lexer.setLine(source.getInitialLine());

			XmlParser parser = new XmlParser(reporter);
			parser.source = source;
			XmlNode result = parser.parse(lexer);

			return new XmlTree<XmlNode>(source, result, list);
		} catch(ParseException ex) {
			/* not parsed */
		} catch(IOException ex) {
			list.add(new XmlProblem(KIND_FATAL, 0, 0, "I/O problem: " + ex.getMessage(), ex));
		}
		return new XmlTree<XmlNode>(source, null, list);
	}


	public static final int KIND_FATAL = 0;
	public static final int KIND_ERROR = 1;
	public static final int KIND_WARN = 2;

	public static final String DEFAULT_SOURCE = "parser";

	public static class XmlProblem extends Exception {
		int kind;
		int start;
		int end;

		private static final long serialVersionUID = 1L;

		public XmlProblem(int kind, int start, int end, String message, Throwable cause) {
			super(message, cause);
			this.kind = kind;
			this.start = start;
			this.end = end;
		}

		public int getKind() {
			return kind;
		}

		public int getStart() {
			return start;
		}

		public int getEnd() {
			return end;
		}

		public String getSource() {
			return DEFAULT_SOURCE;
		}
	}

	public static class TextSource {

		private final String file;
		private final int initialLine;
		private final char[] contents;
		private int[] lineoffset;

		public TextSource(String file, char[] contents, int initialLine) {
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
			return new CharArrayReader(contents);
		}

		public String getLocation(int offset) {
			return file + "," + (initialLine + lineForOffset(offset));
		}

		public String getText(int start, int end) {
			if (start < 0 || start > contents.length || end > contents.length || start > end) {
				return "";
			}
			return new String(contents, start, end - start);
		}

		public int lineForOffset(int offset) {
			if (lineoffset == null) {
				lineoffset = getLineOffsets(contents);
			}
			int line = Arrays.binarySearch(lineoffset, offset);
			return line > 0 ? line : -line - 1;
		}
		
		public char[] getContents() {
			return contents;
		}
	}

	private static int[] getLineOffsets(char[] contents) {
		int size = 1;
		for (int i = 0; i < contents.length; i++) {
			if (contents[i] == '\n') {
				size++;
			} else if(contents[i] == '\r') {
				if(i+1 < contents.length && contents[i+1] == '\n') {
					i++;
				}
				size++;
			}
		}
		int[] result = new int[size];
		result[0] = 0;
		int e = 1;
		for (int i = 0; i < contents.length; i++) {
			if (contents[i] == '\n') {
				result[e++] = i + 1;
			} else if(contents[i] == '\r') {
				if(i+1 < contents.length && contents[i+1] == '\n') {
					i++;
				}
				result[e++] = i + 1;
			}
		}
		if(e != size) {
			throw new IllegalStateException();
		}
		return result;
	}
}
