package net.sf.lapg.gen;

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
import net.sf.lapg.templates.api.impl.TemplateEnvironment;

public class SourceBuilder {

	private static final TargetLanguage[] allLanguages = new TargetLanguage[] {
		new TargetLanguage("java", "java.main", "Parser.java")
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
		return defaults;
	}

	public boolean process(String sourceName, InputStream input, String output) {
		try {
			Grammar s = SyntaxUtil.parseSyntax(sourceName, input, err, getDefaultOptions());
			LexerTables l = LexicalBuilder.compile(s.getLexems(), err, debuglev);
			ParserTables r = Builder.compile(s, err, debuglev);

			// TODO temporary
			if( myLanguage == null ) {
				myLanguage = getLanguage("java"/* TODO s.getOptions().get("lang")*/);
			}

			if (output != null) {
				try {
					PrintStream ps = new PrintStream(output);
					generateOutput(s,l,r,ps);

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

	private void generateOutput(Grammar grammar, LexerTables l, ParserTables r, PrintStream ps) {
		HashMap<String,Object> map = new HashMap<String, Object>();
		map.put("syntax", grammar);
		map.put("lex", l);
		map.put("parser", r);
		map.put("opts", grammar.getOptions() );

		TemplateEnvironment env = new TemplateEnvironment(new GrammarNavigationFactory(), new ClassTemplateLoader(getClass().getClassLoader(), "net/sf/lapg/gen/templates"));
		EvaluationContext context = new EvaluationContext(map);
		context.setVariable("util", new TemplateStaticMethods());
		ps.print(env.executeTemplate(myLanguage.template, context, null));
	}

	public static TargetLanguage getLanguage(String id) {
		for( TargetLanguage l : allLanguages ) {
			if( l.id.equals(id) ) {
				return l;
			}
		}
		return null;
	}
}
