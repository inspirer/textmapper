package net.sf.lapg.gen;

import java.io.InputStream;

import net.sf.lapg.lalr.IError;
import net.sf.lapg.lalr.Result;
import net.sf.lapg.lalr.Syntax;
import net.sf.lapg.lalr.internal.Builder;
import net.sf.lapg.syntax.SyntaxUtils;

public class SourceBuilder {
	
	private static final TargetLanguage[] allLanguages = new TargetLanguage[] {
		new TargetLanguage("java", "", "Parser.java")
	};

	private TargetLanguage myLanguage;
	private int debuglev;
	private IError err;

	public SourceBuilder(TargetLanguage lang, int debug) {
		this.myLanguage = lang;
		this.debuglev = debug;
		this.err = new ErrorImpl(System.err, System.out, System.err);
	}

	public boolean process(String sourceName, InputStream input) {
		Syntax s = SyntaxUtils.parseSyntax(sourceName, input, err);
		Result r = Builder.compile(s.getGrammar(), err, debuglev);
		
		return true;
	}
	
	public static TargetLanguage getLanguage(String id) {
		for( TargetLanguage l : allLanguages ) {
			if( l.id.equals(id) )
				return l;
		}
		return null;
	}
}
