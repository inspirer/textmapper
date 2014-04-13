/**
 * Copyright 2002-2014 Evgeny Gryaznov
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

public class TmaNontermTypeHint extends TmaNode implements ITmaNontermType {

	private final boolean isInline;
	private final TmaNontermTypeHint.TmaKindKind kind;
	private final TmaIdentifier name;
	private final List<TmaSymref> _implements;

	public TmaNontermTypeHint(boolean isInline, TmaNontermTypeHint.TmaKindKind kind, TmaIdentifier name, List<TmaSymref> _implements, TextSource source, int line, int offset, int endoffset) {
		super(source, line, offset, endoffset);
		this.isInline = isInline;
		this.kind = kind;
		this.name = name;
		this._implements = _implements;
	}

	public boolean getIsInline() {
		return isInline;
	}

	public TmaNontermTypeHint.TmaKindKind getKind() {
		return kind;
	}

	public TmaIdentifier getName() {
		return name;
	}

	public List<TmaSymref> getImplements() {
		return _implements;
	}

	@Override
	public void accept(TmaVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (name != null) {
			name.accept(v);
		}
		if (_implements != null) {
			for (TmaSymref it : _implements) {
				it.accept(v);
			}
		}
	}

	public enum TmaKindKind {
		LCLASS,
		LVOID,
		LINTERFACE,
	}
}
