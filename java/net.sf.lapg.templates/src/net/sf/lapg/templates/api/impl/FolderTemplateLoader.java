package net.sf.lapg.templates.api.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import net.sf.lapg.templates.api.ITemplateLoader;

public class FolderTemplateLoader implements ITemplateLoader {

	private File[] myFolders;

	public FolderTemplateLoader(File ...folders) {
		this.myFolders = folders;
	}

	private static String getFileContents(String file) {
		StringBuffer contents = new StringBuffer();
		char[] buffer = new char[2048];
		int count;
		try {
			Reader in = new InputStreamReader(new FileInputStream(file), "utf8");
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

	public String load(String containerName) {
		String fileName = containerName +  CONTAINER_EXT;

		for( File f : myFolders ) {
			File file = new File(f, fileName);
			if( file.exists() ) {
				return getFileContents(file.toString());
			}
		}
		return null;
	}
}
