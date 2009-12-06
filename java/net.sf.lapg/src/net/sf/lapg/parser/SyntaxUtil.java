package net.sf.lapg.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import net.sf.lapg.INotifier;
import net.sf.lapg.api.Grammar;
import net.sf.lapg.parser.LapgTree.ParseProblem;
import net.sf.lapg.parser.LapgTree.TextSource;
import net.sf.lapg.parser.ast.AstRoot;

public class SyntaxUtil {

	public static Grammar parseSyntax(String sourceName, InputStream stream, INotifier err,
			Map<String, String> options) {
		String contents = getFileContents(stream);
		LapgTree<AstRoot> tree = LapgTree.parse(new TextSource(sourceName, contents.toCharArray(), 1));
		Grammar result = null;
		if (!tree.hasErrors()) {
			result = new LapgResolver(tree, options).resolve();
		}
		if (tree.hasErrors()) {
			result = null;
			for (ParseProblem s : tree.getErrors()) {
				err.error(s.getMessage() + "\n");
			}
		}
		return result;

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
