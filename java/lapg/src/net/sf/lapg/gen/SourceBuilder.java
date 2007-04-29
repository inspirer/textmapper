package net.sf.lapg.gen;

import java.io.InputStream;

import net.sf.lapg.IError;
import net.sf.lapg.LexerTables;
import net.sf.lapg.ParserTables;
import net.sf.lapg.Syntax;
import net.sf.lapg.lalr.Builder;
import net.sf.lapg.lex.LexicalBuilder;
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
		ParserTables r = Builder.compile(s.getGrammar(), err, debuglev);
		LexerTables l = LexicalBuilder.compile(s.getLexems(), err);
		
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
