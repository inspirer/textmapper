package net.sf.lapg.syntax;

import java.io.InputStream;
import java.util.Map;

import net.sf.lapg.IError;
import net.sf.lapg.Syntax;

public class SyntaxUtils {

	public static Syntax parseSyntax( String sourceName, InputStream stream, IError err, Map<String,String> options) {
		DescriptionCollector dc = new DescriptionCollector(err, options);
		dc.set_eoi(dc.terminal("eoi", null));

		boolean res = Parser.parse(stream, sourceName, dc, err);

		if (res) {
			dc.set_input(dc.nonterm("input", 0));

			//
		}

		return dc;


	}
}
