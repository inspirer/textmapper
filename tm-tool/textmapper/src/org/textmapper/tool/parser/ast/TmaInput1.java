/**
 * Copyright 2002-2018 Evgeny Gryaznov
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
package org.textmapper.tool.parser.ast;

import java.util.List;
import org.textmapper.tool.parser.TMTree.TextSource;

public class TmaInput1 extends TmaNode {

	private final TmaHeader header;
	private final List<TmaImport> imports;
	private final List<TmaOption> options;
	private final List<ITmaLexerPart> lexer;
	private final List<ITmaGrammarPart> parser;
	private int templatesStart = -1;

	public TmaInput1(TmaHeader header, List<TmaImport> imports, List<TmaOption> options, List<ITmaLexerPart> lexer, List<ITmaGrammarPart> parser, TextSource source, int line, int offset, int endoffset) {
		super(source, line, offset, endoffset);
		this.header = header;
		this.imports = imports;
		this.options = options;
		this.lexer = lexer;
		this.parser = parser;
	}

	public TmaHeader getHeader() {
		return header;
	}

	public List<TmaImport> getImports() {
		return imports;
	}

	public List<TmaOption> getOptions() {
		return options;
	}

	public List<ITmaLexerPart> getLexer() {
		return lexer;
	}

	public List<ITmaGrammarPart> getParser() {
		return parser;
	}

	public int getTemplatesStart() {
		return templatesStart;
	}

	public void setTemplatesStart(int templatesStart) {
		this.templatesStart = templatesStart;
	}

	@Override
	public void accept(TmaVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (header != null) {
			header.accept(v);
		}
		if (imports != null) {
			for (TmaImport it : imports) {
				it.accept(v);
			}
		}
		if (options != null) {
			for (TmaOption it : options) {
				it.accept(v);
			}
		}
		if (lexer != null) {
			for (ITmaLexerPart it : lexer) {
				it.accept(v);
			}
		}
		if (parser != null) {
			for (ITmaGrammarPart it : parser) {
				it.accept(v);
			}
		}
	}
}
