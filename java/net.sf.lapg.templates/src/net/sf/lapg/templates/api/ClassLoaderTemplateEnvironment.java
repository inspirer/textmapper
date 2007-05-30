package net.sf.lapg.templates.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class ClassLoaderTemplateEnvironment extends TemplateEnvironment {

	private ClassLoader loader;
	private String rootPackage;

	public ClassLoaderTemplateEnvironment(ClassLoader loader, String rootPackage) {
		this.loader = loader;
		this.rootPackage = rootPackage;
	}

	protected String getContainerName(String templatePackage) {
		return templatePackage.replace('.', '/') + ".ltp";
	}

	protected String getTemplateContainerContents(String name) {
		InputStream s = loader.getResourceAsStream(rootPackage + "/" + name);
		if(s == null)
			return null;
		
		return getStreamContents(s);
	}

	private static String getStreamContents(InputStream stream) {
		StringBuffer contents = new StringBuffer();
		char[] buffer = new char[2048];
		int count;
		try {
			Reader in = new InputStreamReader(stream);
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
