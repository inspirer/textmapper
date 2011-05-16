package org.textway.templates.ast;


import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.textway.templates.ast.TemplatesLexer.ErrorReporter;
import org.textway.templates.ast.TemplatesParser.ParseException;
import org.textway.templates.bundle.IBundleEntity;

public class TemplatesTree<T> {

	private final TextSource source;
	private final T root;
	private final List<TemplatesProblem> errors;

	public TemplatesTree(TextSource source, T root, List<TemplatesProblem> errors) {
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

	public List<TemplatesProblem> getErrors() {
		return errors;
	}

	public boolean hasErrors() {
		return errors.size() > 0;
	}


	public static TemplatesTree<List<IBundleEntity>> parseInput(TextSource source, String templatePackage) {
		final List<TemplatesProblem> list = new ArrayList<TemplatesProblem>();
		ErrorReporter reporter = new ErrorReporter() {
			public void error(int start, int end, int line, String s) {
				list.add(new TemplatesProblem(KIND_ERROR, start, end, s, null));
			}
		};

		try {
			TemplatesLexer lexer = new TemplatesLexer(source.getStream(), reporter);
			lexer.setLine(source.getInitialLine());

			TemplatesParser parser = new TemplatesParser(reporter);
			parser.source = source;
			parser.templatePackage = templatePackage;
			List<IBundleEntity> result = parser.parseInput(lexer);

			return new TemplatesTree<List<IBundleEntity>>(source, result, list);
		} catch (ParseException ex) {
			/* not parsed */
		} catch (IOException ex) {
			list.add(new TemplatesProblem(KIND_FATAL, 0, 0, "I/O problem: " + ex.getMessage(), ex));
		}
		return new TemplatesTree<List<IBundleEntity>>(source, null, list);
	}

	public static TemplatesTree<TemplateNode> parseBody(TextSource source, String templatePackage) {
		final List<TemplatesProblem> list = new ArrayList<TemplatesProblem>();
		ErrorReporter reporter = new ErrorReporter() {
			public void error(int start, int end, int line, String s) {
				list.add(new TemplatesProblem(KIND_ERROR, start, end, s, null));
			}
		};

		try {
			TemplatesLexer lexer = new TemplatesLexer(source.getStream(), reporter);
			lexer.setLine(source.getInitialLine());

			TemplatesParser parser = new TemplatesParser(reporter);
			parser.source = source;
			parser.templatePackage = templatePackage;
			TemplateNode result = parser.parseBody(lexer);

			return new TemplatesTree<TemplateNode>(source, result, list);
		} catch (ParseException ex) {
			/* not parsed */
		} catch (IOException ex) {
			list.add(new TemplatesProblem(KIND_FATAL, 0, 0, "I/O problem: " + ex.getMessage(), ex));
		}
		return new TemplatesTree<TemplateNode>(source, null, list);
	}


	public static final int KIND_FATAL = 0;
	public static final int KIND_ERROR = 1;
	public static final int KIND_WARN = 2;

	public static final String PARSER_SOURCE = "parser";

	public static class TemplatesProblem extends Exception {
		private static final long serialVersionUID = 1L;

		private final int kind;
		private final int offset;
		private final int endoffset;

		public TemplatesProblem(int kind, int offset, int endoffset, String message, Throwable cause) {
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
			return file + "," + lineForOffset(offset);
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
			return initialLine + (line >= 0 ? line : -line - 2);
		}

		public int columnForOffset(int offset) {
			if (lineoffset == null) {
				lineoffset = getLineOffsets(contents);
			}
			int line = Arrays.binarySearch(lineoffset, offset);
			return offset >= 0 ? offset - lineoffset[line >= 0 ? line : -line - 2] : 0;
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
			} else if (contents[i] == '\r') {
				if (i + 1 < contents.length && contents[i + 1] == '\n') {
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
			} else if (contents[i] == '\r') {
				if (i + 1 < contents.length && contents[i + 1] == '\n') {
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
