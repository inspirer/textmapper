/**
 * Copyright 2002-2019 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.textmapper.tool.bootstrap.a;

import org.textmapper.lapg.eval.GenericNode;
import org.textmapper.lapg.eval.GenericParseContext;
import org.textmapper.lapg.eval.GenericParseContext.ParseProblem;
import org.textmapper.lapg.eval.GenericParseContext.Result;
import org.textmapper.tool.bootstrap.a.SampleALexer.ErrorReporter;
import org.textmapper.tool.bootstrap.a.SampleAParser.ParseException;
import org.textmapper.tool.bootstrap.a.SampleATree.TextSource;
import org.textmapper.tool.bootstrap.a.ast.AstClassdef;
import org.textmapper.tool.bootstrap.a.ast.AstClassdeflistItem;
import org.textmapper.tool.bootstrap.a.ast.IAstClassdefNoEoi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Gryaznov Evgeny, 4/3/11
 */
public class SampleAParseContext {

	public Result parse(String text, boolean eoi) {
		TextSource source = new TextSource("input", text, 1);
		org.textmapper.lapg.eval.GenericParseContext.TextSource source2 = new org.textmapper.lapg.eval.GenericParseContext.TextSource(source.getFile(), source.getContents(), 1);
		final List<ParseProblem> list = new ArrayList<>();
		ErrorReporter reporter = (message, line, offset, column, endline, endoffset, endcolumn) -> list.add(new ParseProblem(GenericParseContext.KIND_ERROR, offset, endoffset, message, null));

		try {
			SampleALexer lexer = new SampleALexer(source.getContents(), reporter);
			lexer.setLine(source.getInitialLine());

			SampleAParser parser = new SampleAParser(reporter);
			IAstClassdefNoEoi result = eoi ? parser.parseClassdef(lexer) : parser.parseClassdef_no_eoi(lexer);

			return new Result(source2, convert(source2, result), list);
		} catch (ParseException ex) {
			/* not parsed */
		} catch (IOException ex) {
			list.add(new ParseProblem(SampleATree.KIND_FATAL, 0, 0, "I/O problem: " + ex.getMessage(), ex));
		}
		return new Result(source2, null, list);
	}

	public GenericNode convert(org.textmapper.lapg.eval.GenericParseContext.TextSource source, IAstClassdefNoEoi cd) {
		AstClassdef def = (AstClassdef) cd;
		GenericNode[] children = new GenericNode[def.getClassdeflist() != null ? def.getClassdeflist().size() : 0];
		if (def.getClassdeflist() != null) {
			int i = 0;
			for (AstClassdeflistItem c : def.getClassdeflist()) {
				children[i++] = convert(source, c.getClassdef());
			}
		}
		return new GenericNode(source, def.getOffset(), def.getEndoffset(), children);
	}
}
