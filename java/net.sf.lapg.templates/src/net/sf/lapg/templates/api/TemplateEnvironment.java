package net.sf.lapg.templates.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;

import net.sf.lapg.templates.parser.Parser;

public class TemplateEnvironment extends DefaultEnvironment {
	
	private File[] myFolders;
	private HashMap<String,ITemplate> templates;

	public TemplateEnvironment(File[] folders) {
		this.myFolders = folders;
		this.templates = new HashMap<String,ITemplate>();
	}
	
	private File getFile(String name) {
		for( File f : myFolders ) {
			File file = new File(f, name);
			if( file.exists() )
				return file;
		}
		return null;
	}
	
	private ITemplate getTemplate(String name) {
		if( templates.containsKey(name) )
			return templates.get(name);

		int lastDot = name.lastIndexOf('.');
		if( lastDot == -1 ) {
			fireError("Fully qualified template name should contain dot.");
			return null;
		}
		
		String fname = name.substring(0, lastDot);
		File f = getFile(fname+".ltp");
		if( f == null ) {
			fireError("Couldn't find template file for template `" + name + "`");
			return null;
		}

		ITemplate[] loaded = loadTemplatesFromFile(f.toString(), fname);
		if( loaded == null || loaded.length == 0 ) {
			fireError("Couldn't load templates from file " + f.toString());
			return null;
		}

		String id = name.substring(lastDot+1);
		ITemplate result = null;
		for( ITemplate t : loaded ) {
			if( t.getName().equals(id))
				result = t;
			templates.put(fname+"."+t.getName(), t);
		}
		
		if( result == null ) {
			fireError("Template `"+id+"` was not found in file `" + f.toString() + "`");
		}
		return result;
	}

	public String executeTemplate(String name, Object context, Object[] arguments) {
		ITemplate t = getTemplate(name);
		if( t == null )
			return "";
		try {
			return t.apply(context, this, arguments);
		} catch( EvaluationException ex ) {
			fireError(ex.getMessage());
			return "";
		}
	}

	private static ITemplate[] loadTemplates(String templates, String templatePackage) {
		Parser p = new Parser();
		if( !p.parse(templates, templatePackage) )
			return new ITemplate[0];
		return p.getResult();
	}
	
	private static ITemplate[] loadTemplatesFromFile(String filename, String templatePackage) {
		String contents = getFileContents(filename);
		if( contents == null )
			return null;
		
		return loadTemplates(contents, templatePackage);
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
