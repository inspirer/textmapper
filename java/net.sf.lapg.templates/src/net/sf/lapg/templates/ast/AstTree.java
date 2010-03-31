package net.sf.lapg.templates.ast;


import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import net.sf.lapg.templates.ast.AstLexer.ErrorReporter;
import net.sf.lapg.templates.ast.AstParser.ParseException;

public class AstTree<T> {

	private final TextSource source;
	private final T root;
	private final List<AstProblem> errors;

	public AstTree(TextSource source, T root, List<AstProblem> errors) {
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

	public List<AstProblem> getErrors() {
		return errors;
	}

	public boolean hasErrors() {
		return errors.size() > 0;
	}


	public static AstTree<Object> parseInput(TextSource source) {
		final List<AstProblem> list = new ArrayList<AstProblem>();
		ErrorReporter reporter = new ErrorReporter() {
			public void error(int start, int end, int line, String s) {
				list.add(new AstProblem(KIND_ERROR, start, end, s, null));
			}
		};

		try {
			AstLexer lexer = new AstLexer(source.getStream(), reporter);
			lexer.setLine(source.getInitialLine());

			AstParser parser = new AstParser(reporter);
			Object result = parser.parseInput(lexer);

			return new AstTree<Object>(source, result, list);
		} catch(ParseException ex) {
			/* not parsed */
		} catch(IOException ex) {
			list.add(new AstProblem(KIND_FATAL, 0, 0, "I/O problem: " + ex.getMessage(), ex));
		}
		return new AstTree<Object>(source, null, list);
	}

	public static AstTree<TemplateNode> parseBody(TextSource source) {
		final List<AstProblem> list = new ArrayList<AstProblem>();
		ErrorReporter reporter = new ErrorReporter() {
			public void error(int start, int end, int line, String s) {
				list.add(new AstProblem(KIND_ERROR, start, end, s, null));
			}
		};

		try {
			AstLexer lexer = new AstLexer(source.getStream(), reporter);
			lexer.setLine(source.getInitialLine());

			AstParser parser = new AstParser(reporter);
			TemplateNode result = parser.parseBody(lexer);

			return new AstTree<TemplateNode>(source, result, list);
		} catch(ParseException ex) {
			/* not parsed */
		} catch(IOException ex) {
			list.add(new AstProblem(KIND_FATAL, 0, 0, "I/O problem: " + ex.getMessage(), ex));
		}
		return new AstTree<TemplateNode>(source, null, list);
	}


	public static final int KIND_FATAL = 0;
	public static final int KIND_ERROR = 1;
	public static final int KIND_WARN = 2;

	public static final String DEFAULT_SOURCE = "parser";

	public static class AstProblem extends Exception {
		int kind;
		int start;
		int end;

		private static final long serialVersionUID = 1L;

		public AstProblem(int kind, int start, int end, String message, Throwable cause) {
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
