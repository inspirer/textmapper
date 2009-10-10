package net.sf.lapg.input;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import net.sf.lapg.INotifier;
import net.sf.lapg.api.Grammar;

public class SyntaxUtil {

	public static Grammar parseSyntax(String sourceName, InputStream stream, INotifier err, Map<String, String> options) {
		String contents = getFileContents(stream);
		CSyntax cs = LapgParser.process(sourceName, contents, options);
		if (cs.hasErrors()) {
			for (String s : cs.getErrors()) {
				err.error(s + "\n");
			}
		}
		return cs;
	}

	public static String getFileContents(InputStream stream) {
		StringBuffer contents = new StringBuffer();
		char[] buffer = new char[2048];
		int count;
		try {
			Reader in = new InputStreamReader(stream, "utf8");
			try {
				while ((count = in.read(buffer)) > 0) {
					contents.append(buffer, 0, count);
				}
			} finally {
				in.close();
			}
		} catch (IOException ioe) {
			return null;
		}
		return contents.toString();
	}

	public static int[] getLineOffsets(char[] contents) {
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
