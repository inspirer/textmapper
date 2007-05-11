package net.sf.lapg.templates;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import net.sf.lapg.templates.api.DefaultEnvironment;
import net.sf.lapg.templates.api.EvaluationException;
import net.sf.lapg.templates.api.IEvaluationEnvironment;
import net.sf.lapg.templates.api.ITemplate;
import net.sf.lapg.templates.parser.Parser;

public class TemplateService {

	private TemplateService() {
	}

	public static String executeTemplate(ITemplate template, Object context, Object[] arguments) {
		IEvaluationEnvironment env = new DefaultEnvironment();
		try {
			return template.apply(context, env, arguments);
		} catch( EvaluationException ex ) {
			env.fireError(ex.getMessage());
			return "";
		}
	}

	public static ITemplate[] loadTemplates(String templates) {
		Parser p = new Parser();
		if( !p.parse(templates) )
			return new ITemplate[0];
		return p.getResult();
	}
	
	public static ITemplate[] loadTemplatesFromFile(String filename) {
		String contents = getFileContents(filename);
		if( contents == null )
			return null;
		
		return loadTemplates(contents);
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
