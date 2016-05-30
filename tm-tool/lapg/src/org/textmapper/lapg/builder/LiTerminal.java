/**
 * Copyright 2002-2016 Evgeny Gryaznov
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
package org.textmapper.lapg.builder;

import org.textmapper.lapg.api.LexerRule;
import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.Terminal;
import org.textmapper.lapg.api.ast.AstType;
import org.textmapper.lapg.api.regex.RegexPart;

import java.util.ArrayList;
import java.util.List;

/**
 * evgeny, 11/16/12
 */
public class LiTerminal extends LiSymbol implements Terminal {

	private Symbol softClass;
	private List<LexerRule> rules = new ArrayList<>();

	public LiTerminal(String name, AstType type, SourceElement origin) {
		super(name, false /* anonymous */, origin);
		setType(type);
	}

	@Override
	public boolean isTerm() {
		return true;
	}

	@Override
	public Symbol getSoftClass() {
		return softClass;
	}

	@Override
	public Iterable<LexerRule> getRules() {
		return rules;
	}

	void addRule(LexerRule rule) {
		rules.add(rule);
	}

	void setSoftClass(Terminal sc) {
		assert softClass == null;
		assert getType() == null;
		assert !sc.isSoft();

		softClass = sc;
	}

	@Override
	public AstType getType() {
		if (softClass != null) {
			return softClass.getType();
		}
		return super.getType();
	}

	@Override
	public boolean isSoft() {
		return softClass != null;
	}

	@Override
	public String getConstantValue() {
		String value = null;
		for (LexerRule rule : rules) {
			final RegexPart regex = rule.getRegexp();
			final String regexValue = regex.getConstantValue();
			if (regexValue == null) {
				return null;
			}
			if (value == null) {
				value = regexValue;
			} else if (!value.equals(regexValue)) {
				return null;
			}
		}

		return (isSoft() || getType() == null) ? value : null;
	}

	@Override
	public boolean isConstant() {
		return getConstantValue() != null;
	}
}

