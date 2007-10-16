package net.sf.lapg.gen;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;

import net.sf.lapg.IError;
import net.sf.lapg.LexerTables;
import net.sf.lapg.ParserTables;
import net.sf.lapg.Syntax;
import net.sf.lapg.lalr.Builder;
import net.sf.lapg.lex.LexicalBuilder;
import net.sf.lapg.syntax.SyntaxUtils;
import net.sf.lapg.templates.api.ClassLoaderTemplateEnvironment;
import net.sf.lapg.templates.api.IStaticMethods;
import net.sf.lapg.templates.api.TemplateEnvironment;

public class SourceBuilder {

	private static final TargetLanguage[] allLanguages = new TargetLanguage[] {
		new TargetLanguage("java", "", "Parser.java")
	};

	private TargetLanguage myLanguage;
	private int debuglev;
	private IError err;

	public SourceBuilder(TargetLanguage lang, int debuglev) {
		this.myLanguage = lang;
		this.debuglev = debuglev;
		this.err = new ErrorImpl(debuglev);
	}

	public boolean process(String sourceName, InputStream input, String output) {
		try {
			Syntax s = SyntaxUtils.parseSyntax(sourceName, input, err);
			LexerTables l = LexicalBuilder.compile(s.getLexems(), err, debuglev);
			ParserTables r = Builder.compile(s.getGrammar(), err, debuglev);

			// temporary
			if (output != null) {
				try {
					PrintStream ps = new PrintStream(output);
//					OutputUtils.printTables(ps, l);
//					OutputUtils.printTables(ps, r);
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

	private void generateOutput(Syntax s, LexerTables l, ParserTables r, PrintStream ps) {
		HashMap<String,Object> map = new HashMap<String, Object>();
		map.put("syntax", s);
		map.put("lex", l);
		map.put("parser", r);
		map.put("opts", s.getOptions() );

		TemplateEnvironment env = new ClassLoaderTemplateEnvironment(getClass().getClassLoader(), "net/sf/lapg/gen/templates") {
			private IStaticMethods myStaticMethods = null;

			@Override
			public IStaticMethods getStaticMethods() {
				if( myStaticMethods == null) {
					myStaticMethods = new TemplateStaticMethods();
				}
				return myStaticMethods;
			}
		};
		ps.print(env.executeTemplate("java.main", map, null));
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
