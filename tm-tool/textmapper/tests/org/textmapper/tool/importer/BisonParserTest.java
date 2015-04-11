/**
 * Copyright 2002-2015 Evgeny Gryaznov
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
package org.textmapper.tool.importer;

import org.junit.Test;
import org.textmapper.tool.importer.BisonTree.TextSource;

import static org.junit.Assert.assertFalse;

public class BisonParserTest {
	@Test
	public void testLookahead() throws Exception {
		BisonTree<Object> result = BisonTree.parse(new TextSource("input",
				"/* comment */\n" +
						"\n" +
						"%{\n" +
						"#define YYSTYPE int\n" +
						"#include <stdio.h>\n" +
						"%}\n" +
						"\n" +
						"%token NUM\n" +
						"%left '+'\n" +
						"%left '*'\n" +
						"\n" +
						"%% /* Grammar */\n" +
						"\n" +
						"input:    /* empty */\n" +
						"        | input line\n" +
						";\n" +
						"\n" +
						"line:     '\\n'\n" +
						"        | expr '\\n'  { do { nested(\"}\"); } while(0); }\n" +
						";\n" +
						"\n" +
						"expr    : NUM               { $$ = $1;         }\n" +
						"        | expr '+' expr     { $$ = $1 + $3;    }\n" +
						"        | expr '*' expr     { $$ = $1 * $3;    }\n" +
						";\n" +
						"%%", 1));
		assertFalse(result.hasErrors());
	}
}