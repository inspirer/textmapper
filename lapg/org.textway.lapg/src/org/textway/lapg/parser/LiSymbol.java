/**
 * Copyright 2002-2011 Evgeny Gryaznov
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
package org.textway.lapg.parser;

import org.textway.lapg.api.Symbol;
import org.textway.lapg.parser.ast.IAstNode;

public class LiSymbol extends LiAnnotated implements Symbol {

	private final int kind;
	private final String name;
	private int index;
	private String identifier;
	private String type;
	private Symbol softClass;

	public LiSymbol(int kind, String name, String type, IAstNode node) {
		super(null, node);
		this.kind = kind;
		this.name = name;
		this.type = type;
	}

	@Override
	public int getKind() {
		return kind;
	}

	public int getIndex() {
		return index;
	}

	public void setId(int index, String identifier) {
		this.index = index;
		this.identifier = identifier;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	void setType(String type) {
		this.type = type;
	}

	public boolean isTerm() {
		return kind == KIND_TERM || kind == KIND_SOFTTERM;
	}

	public boolean isSoft() {
		return kind == KIND_SOFTTERM;
	}

	public Symbol getSoftClass() {
		return softClass;
	}

	void setSoftClass(Symbol softClass) {
		this.softClass = softClass;
	}

	public String getId() {
		return identifier;
	}

	public String kindAsString() {
		switch (kind) {
			case KIND_TERM:
				return "terminal";
			case KIND_SOFTTERM:
				return "soft-terminal";
			case KIND_NONTERM:
				return "non-terminal";
			case KIND_LAYOUT:
				return "layout";
		}
		return "<unknown>";
	}
}
