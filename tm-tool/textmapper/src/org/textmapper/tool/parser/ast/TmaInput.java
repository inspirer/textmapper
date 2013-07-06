/**
 * Copyright 2002-2013 Evgeny Gryaznov
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

import org.textmapper.tool.parser.TMTree.TextSource;

import java.util.List;

public class TmaInput extends TmaNode {

	private final List<TmaOptionPart> options;
	private final List<TmaLexerPart> lexer;
	private final List<TmaGrammarPart> grammar;
	private int templatesStart = -1;

	public TmaInput(List<TmaOptionPart> options, List<TmaLexerPart> lexer, List<TmaGrammarPart> grammar, TextSource source,
					int offset, int endoffset) {
		super(source, offset, endoffset);
		this.options = options;
		this.lexer = lexer;
		this.grammar = grammar;
	}

	public List<TmaOptionPart> getOptions() {
		return options;
	}

	public List<TmaLexerPart> getLexer() {
		return lexer;
	}

	public List<TmaGrammarPart> getGrammar() {
		return grammar;
	}

	public int getTemplatesStart() {
		return templatesStart;
	}

	public void setTemplatesStart(int templatesStart) {
		this.templatesStart = templatesStart;
	}

	public void accept(TmaVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (options != null) {
			for (TmaOptionPart o : options) {
				o.accept(v);
			}
		}
		if (lexer != null) {
			for (TmaLexerPart l : lexer) {
				l.accept(v);
			}
		}
		if (grammar != null) {
			for (TmaGrammarPart g : grammar) {
				g.accept(v);
			}
		}
	}
}
