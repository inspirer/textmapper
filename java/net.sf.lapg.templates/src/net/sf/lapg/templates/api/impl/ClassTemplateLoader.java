package net.sf.lapg.templates.api.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import net.sf.lapg.templates.api.ITemplateLoader;

public class ClassTemplateLoader implements ITemplateLoader {

	private ClassLoader loader;

	private String rootPackage;

	public ClassTemplateLoader(ClassLoader loader, String rootPackage) {
		this.loader = loader;
		this.rootPackage = rootPackage;
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

	public String load(String containerName) {
		InputStream s = loader.getResourceAsStream(rootPackage + "/" + (containerName.replace('.', '/') + CONTAINER_EXT));
		if (s == null) {
			return null;
		}

		return getStreamContents(s);
	}
}
