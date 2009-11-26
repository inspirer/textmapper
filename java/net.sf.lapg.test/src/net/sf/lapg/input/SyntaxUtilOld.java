package net.sf.lapg.input;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import net.sf.lapg.INotifier;
import net.sf.lapg.api.Grammar;
import net.sf.lapg.parser.LapgResolver;
import net.sf.lapg.parser.LapgTree;
import net.sf.lapg.parser.LapgTree.ParseProblem;
import net.sf.lapg.parser.LapgTree.TextSource;

public class SyntaxUtilOld {

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
}
