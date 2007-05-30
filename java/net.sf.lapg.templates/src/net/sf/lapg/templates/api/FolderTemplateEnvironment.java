package net.sf.lapg.templates.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

public class FolderTemplateEnvironment extends TemplateEnvironment {
	
	private File[] myFolders;

	public FolderTemplateEnvironment(File[] folders) {
		this.myFolders = folders;
	}

	protected String getTemplateContainerContents(String name) {
		for( File f : myFolders ) {
			File file = new File(f, name);
			if( file.exists() ) {
				return getFileContents(file.toString());
			}
		}
		return null;
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

	protected String getContainerName(String templatePackage) {
		return templatePackage + ".ltp";
	}
}
