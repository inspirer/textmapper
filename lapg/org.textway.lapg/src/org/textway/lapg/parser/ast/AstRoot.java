/**
 * Copyright 2002-2010 Evgeny Gryaznov
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
package org.textway.lapg.parser.ast;

import org.textway.lapg.parser.LapgTree.TextSource;

import java.util.List;

public class AstRoot extends AstNode {

	private final List<AstOptionPart> options;
	private final List<AstLexerPart> lexer;
	private final List<AstGrammarPart> grammar;
	private int templatesStart = -1;

	public AstRoot(List<AstOptionPart> options, List<AstLexerPart> lexer, List<AstGrammarPart> grammar, TextSource source,
			int offset, int endoffset) {
		super(source, offset, endoffset);
		this.options = options;
		this.lexer = lexer;
		this.grammar = grammar;
	}

	public List<AstOptionPart> getOptions() {
		return options;
	}

	public List<AstLexerPart> getLexer() {
		return lexer;
	}

	public List<AstGrammarPart> getGrammar() {
		return grammar;
	}

	public int getTemplatesStart() {
		return templatesStart;
	}

	public void setTemplatesStart(int templatesStart) {
		this.templatesStart = templatesStart;
	}

	public void accept(AbstractVisitor v) {
		if(!v.visit(this)) {
			return;
		}
		if(options != null) {
			for(AstOptionPart o : options) {
				o.accept(v);
			}
		}
		if(lexer != null) {
			for(AstLexerPart l : lexer) {
				l.accept(v);
			}
		}
		if(grammar != null) {
			for(AstGrammarPart g : grammar) {
				g.accept(v);
			}
		}
	}
}
