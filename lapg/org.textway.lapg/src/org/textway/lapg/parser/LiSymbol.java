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
package org.textway.lapg.parser;

import org.textway.lapg.api.Symbol;
import org.textway.lapg.parser.ast.IAstNode;

public class LiSymbol extends LiAnnotated implements Symbol {

	private int index;
    private String identifier;
	private final String name;
	private final String type;
	private final boolean isTerm;

	public LiSymbol(String name, String type, boolean isTerm, IAstNode node) {
		super(null, node);
		this.name = name;
		this.type = type;
		this.isTerm = isTerm;
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

	public boolean isDefined() {
		return true;
	}

	public boolean isTerm() {
		return isTerm;
	}

    public String getId() {
        return identifier;
    }
}
