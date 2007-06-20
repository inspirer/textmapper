package net.sf.lapg.syntax;

import java.io.InputStream;

import net.sf.lapg.IError;
import net.sf.lapg.Syntax;

public class SyntaxUtils {

	public static Syntax parseSyntax( String sourceName, InputStream stream, IError err ) {
		DescriptionCollector dc = new DescriptionCollector(err);
		dc.set_eoi(dc.terminal("eoi", null));

		boolean res = Parser.parse(stream, sourceName, dc, err);

		if (res) {
			dc.set_input(dc.nonterm("input", 0));

			// 
		}

		return dc;

		
	}
}
