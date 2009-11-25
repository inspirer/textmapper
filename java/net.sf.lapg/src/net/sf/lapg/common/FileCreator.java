package net.sf.lapg.common;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FileCreator {

	private final String name;
	private final String contents;
	private final String charset;
	
	public static String NL = System.getProperty("line.separator");
	
	public FileCreator(String name, String contents, String charset) {
		this.name = name;
		this.contents = contents;
		this.charset = charset;
		
	}
		
	protected String getData() {
		return fixLineSeparators(contents);
	}
	
	protected String getName() throws IOException {
		// TODO replace slashes, create folders
		return name;
	}
	
	public void create() throws IOException {
		OutputStream os = new FileOutputStream(new File(getName()));
		String data = getData();
		os.write(data.getBytes(charset));
		os.close();
	}

	private static String fixLineSeparators(String contents) {
		StringBuilder sb = new StringBuilder(contents.length());
		int size = contents.length();
		for(int i = 0; i < size; i++) {
			char c = contents.charAt(i);
			if(c == '\n') {
				sb.append(NL);
			} else if(c == '\r') {
				sb.append(NL);
				if(i+1 < size && contents.charAt(i+1) == '\n') {
					i++;
				}
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}
}
