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
package org.textmapper.lapg.builder;

import org.textmapper.lapg.api.Nonterminal;
import org.textmapper.lapg.api.TemplateEnvironment;

/**
 * An immutable descriptor of an instantiated template.
 */
class InstanceKey {
	final Nonterminal nonterminal;
	final TemplateEnvironment environment;

	public InstanceKey(Nonterminal nonterminal, TemplateEnvironment environment) {
		this.nonterminal = nonterminal;
		this.environment = environment;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		InstanceKey that = (InstanceKey) o;
		return nonterminal.equals(that.nonterminal) && environment.equals(that.environment);

	}

	@Override
	public int hashCode() {
		int result = nonterminal.hashCode();
		result = 31 * result + environment.hashCode();
		return result;
	}
}
