package net.sf.lapg.parser;

import java.util.Arrays;

import net.sf.lapg.input.SyntaxUtil;

public class TextSource {

	private String file;
	private int initialLine;
	private char[] contents;
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
			lineoffset = SyntaxUtil.getLineOffsets(contents);
		}
		int line = Arrays.binarySearch(lineoffset, offset);
		return line > 0 ? line : -line - 1;
	}
}
