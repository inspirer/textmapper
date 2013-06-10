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
package org.textmapper.lapg.builder;

import org.textmapper.lapg.api.ast.AstField;
import org.textmapper.lapg.api.rule.RhsMapping;

/**
 * evgeny, 2/25/13
 */
class LiRhsMapping implements RhsMapping {

	public static final RhsMapping EMPTY_MAPPING = new LiRhsMapping(null, null, false);

	private final AstField field;
	private final Object value;
	private final boolean isAddition;

	public LiRhsMapping(AstField field, Object value, boolean addition) {
		this.field = field;
		this.value = value;
		isAddition = addition;
	}

	@Override
	public AstField getField() {
		return field;
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public boolean isAddition() {
		return isAddition;
	}
}
