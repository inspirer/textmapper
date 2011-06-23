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

import org.textway.lapg.api.NamedPattern;
import org.textway.lapg.parser.ast.IAstNode;
import org.textway.lapg.regex.RegexPart;
import org.textway.templates.api.INamedEntity;

/**
 * Gryaznov Evgeny, 6/23/11
 */
public class LiNamedPattern extends LiEntity implements NamedPattern, INamedEntity {

	private final String name;
	private final RegexPart regexp;

	public LiNamedPattern(String name, RegexPart regexp, IAstNode node) {
		super(node);
		this.name = name;
		this.regexp = regexp;
	}

	public String getName() {
		return name;
	}

	public String getRegexp() {
		return regexp.toString();
	}

	public RegexPart getParsedRegexp() {
		return regexp;
	}

	public String getTitle() {
		return "Pattern `" + name + "`";
	}
}
