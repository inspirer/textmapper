package net.sf.lapg.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class FileUtil {

	public static final String DEFAULT_ENCODING = "utf8";

	public static String getFileContents(InputStream stream, String encoding) {
		StringBuilder contents = new StringBuilder();
		char[] buffer = new char[2048];
		int count;
		try {
			Reader in = new InputStreamReader(stream, encoding);
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
