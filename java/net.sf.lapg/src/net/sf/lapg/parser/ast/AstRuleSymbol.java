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
package net.sf.lapg.parser.ast;

import net.sf.lapg.parser.LapgTree.TextSource;

public class AstRuleSymbol extends AstNode {

	private final AstCode action;
	private final String alias;
	private final AstReference symbol;
	private final AstAnnotations annotations;

	private final AstError error;

	public AstRuleSymbol(AstCode action, String alias, AstReference symbol, AstAnnotations annotations, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.action = action;
		this.alias = alias;
		this.symbol = symbol;
		this.annotations = annotations;
		this.error = null;
	}

	public AstRuleSymbol(AstError error) {
		super(error.getInput(), error.getOffset(), error.getEndOffset());
		this.action = null;
		this.alias = null;
		this.symbol = null;
		this.annotations = null;
		this.error = error;
	}

	public boolean hasSyntaxError() {
		return error != null;
	}

	public AstCode getCode() {
		return action;
	}

	public AstReference getSymbol() {
		return symbol;
	}

	public String getAlias() {
		return alias;
	}

	public AstAnnotations getAnnotations() {
		return annotations;
	}

	public void accept(AbstractVisitor v) {
		if(error != null) {
			v.visit(error);
			return;
		}
		if(!v.visit(this)) {
			return;
		}
		if(symbol != null) {
			symbol.accept(v);
		}
		if(annotations != null) {
			annotations.accept(v);
		}
		if(action != null) {
			action.accept(v);
		}
	}
}
