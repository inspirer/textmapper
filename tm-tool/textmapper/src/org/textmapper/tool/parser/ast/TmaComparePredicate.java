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

import org.textmapper.tool.parser.TMTree.TextSource;

public class TmaComparePredicate extends TmaNode implements ITmaPredicateExpression {

	private final TmaParamRef paramRef;
	private final TmaComparePredicate.TmaKindKind kind;
	private final TmaLiteral literal;

	public TmaComparePredicate(TmaParamRef paramRef, TmaComparePredicate.TmaKindKind kind, TmaLiteral literal, TextSource source, int line, int offset, int endoffset) {
		super(source, line, offset, endoffset);
		this.paramRef = paramRef;
		this.kind = kind;
		this.literal = literal;
	}

	public TmaParamRef getParamRef() {
		return paramRef;
	}

	public TmaComparePredicate.TmaKindKind getKind() {
		return kind;
	}

	public TmaLiteral getLiteral() {
		return literal;
	}

	@Override
	public void accept(TmaVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (paramRef != null) {
			paramRef.accept(v);
		}
		if (literal != null) {
			literal.accept(v);
		}
	}

	public enum TmaKindKind {
		ASSIGN_ASSIGN,
		EXCL_ASSIGN,
	}
}
