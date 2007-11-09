package net.sf.lapg.input;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class InputTest {

	public static void main(String[] args) {
		String toParse = getFileContents(args[0]);
		CSyntax cs = Parser.process(toParse);
		if( cs.hasErrors() ) {
			for( String s : cs.getErrors()) {
				System.err.println(s);
			}
		}
	}

	private static String getFileContents(String file) {
		StringBuffer contents = new StringBuffer();
		char[] buffer = new char[2048];
		int count;
		try {
			Reader in = new InputStreamReader(new FileInputStream(file));
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
