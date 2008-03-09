package net.sf.lapg.gen;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import net.sf.lapg.IError;
import net.sf.lapg.LexerTables;
import net.sf.lapg.ParserTables;
import net.sf.lapg.api.Grammar;
import net.sf.lapg.input.SyntaxUtil;
import net.sf.lapg.lalr.Builder;
import net.sf.lapg.lex.LexicalBuilder;
import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.impl.ClassTemplateLoader;
import net.sf.lapg.templates.api.impl.FolderTemplateLoader;
import net.sf.lapg.templates.api.impl.StringTemplateLoader;
import net.sf.lapg.templates.api.impl.TemplateEnvironment;

public class SourceBuilder {

	private static final TargetLanguage[] allLanguages = new TargetLanguage[] {
		new TargetLanguage("java", "java")
	};

	private TargetLanguage myLanguage;
	private int debuglev;
	private IError err;

	public SourceBuilder(TargetLanguage lang, int debuglev) {
		this.myLanguage = lang;
		this.debuglev = debuglev;
		this.err = new ErrorImpl(debuglev);
	}

	private Map<String,String> getDefaultOptions() {
		HashMap<String, String> defaults = new HashMap<String, String>();
		defaults.put("class","Parser");
		defaults.put("getsym","?");
		defaults.put("errorprefix","");
		defaults.put("breaks","on");
		defaults.put("namespace","?");
		defaults.put("lang","java");
		defaults.put("positioning","line");
		defaults.put("lexemend","off");
		defaults.put("maxtoken","2048");
		defaults.put("stack","1024");
		defaults.put("packLexems","false");
		return defaults;
	}

	public boolean process(String sourceName, InputStream input, String output, String template) {
		try {
			Grammar s = SyntaxUtil.parseSyntax(sourceName, input, err, getDefaultOptions());
			// TODO check compilability
			LexerTables l = LexicalBuilder.compile(s.getLexems(), err, debuglev);
			ParserTables r = Builder.compile(s, err, debuglev);

			// TODO temporary
			if( myLanguage == null ) {
				myLanguage = getLanguage("java"/* TODO s.getOptions().get("lang")*/);
			}

			// TODO generate Parser.java if output == null

			if (output != null) {
				try {
					PrintStream ps = new PrintStream(output);
					generateOutput(s,l,r,ps,template);

				} catch (FileNotFoundException ex) {
					err.error(ex.getLocalizedMessage());
				}
			}

			return true;
		} catch (Throwable t) {
			err.error("lapg: internal error: " + t.getClass().getName()+"\n");
			if( debuglev >= 2) {
				t.printStackTrace(System.err);
			}
			return false;
		}
	}

	private void generateOutput(Grammar grammar, LexerTables l, ParserTables r, PrintStream ps, String template) {
		HashMap<String,Object> map = new HashMap<String, Object>();
		map.put("syntax", grammar);
		map.put("lex", l);
		map.put("parser", r);
		map.put("opts", grammar.getOptions() );

		String templatePackage = template != null ? template : myLanguage.getTemplatePackage();
		TemplateEnvironment env = new TemplateEnvironment(
				new GrammarNavigationFactory(templatePackage),
				new StringTemplateLoader("input", grammar.getTemplates()), // TODO create with initial location
				template != null ?
						new FolderTemplateLoader((File)null)
						: new ClassTemplateLoader(getClass().getClassLoader(), "net/sf/lapg/gen/templates"));
		env.loadPackage(null, "input");
		EvaluationContext context = new EvaluationContext(map);
		context.setVariable("util", new TemplateStaticMethods());
		context.setVariable("$", "lapg_gg.sym"); // TODO remove hack
		ps.print(env.executeTemplate(templatePackage+".main", context, null));
	}

	public static TargetLanguage getLanguage(String id) {
		for( TargetLanguage l : allLanguages ) {
			if( l.getId().equals(id) ) {
				return l;
			}
		}
		return null;
	}
}
