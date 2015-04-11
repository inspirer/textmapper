/**
 * Copyright 2002-2015 Evgeny Gryaznov
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
import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.TemplateEnvironment;
import org.textmapper.lapg.api.Terminal;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

class TemplateInstance {

	private final LiNonterminal template;
	private final TemplateEnvironment environment;
	private final SourceElement referrer;
	private Map<TemplatedSymbolRef, TemplateInstance> targetInstance;
	private Map<TemplatedSymbolRef, Terminal> terminals;

	TemplateInstance(LiNonterminal template, TemplateEnvironment environment, SourceElement referrer) {
		this.template = template;
		this.environment = environment;
		this.referrer = referrer;
		template.addInstance(this);
	}

	LiNonterminal getTemplate() {
		return template;
	}

	TemplateEnvironment getEnvironment() {
		return environment;
	}

	SourceElement getReferrer() {
		return referrer;
	}

	void addNonterminalTarget(TemplatedSymbolRef ref, TemplateInstance target) {
		if (targetInstance == null) {
			targetInstance = new HashMap<TemplatedSymbolRef, TemplateInstance>();
		}
		targetInstance.put(ref, target);
	}

	void addTerminalTarget(TemplatedSymbolRef ref, Terminal target) {
		if (terminals == null) {
			terminals = new HashMap<TemplatedSymbolRef, Terminal>();
		}
		terminals.put(ref, target);
	}

	Nonterminal getOrCreateNonterminal() {
		if (template.isTemplate()) {
			// TODO create and copy
			throw new UnsupportedOperationException("templates are not fully working yet");
		}

		return template;
	}

	private void updateExistingNonterminal() {
		if (targetInstance != null) {
			for (Entry<TemplatedSymbolRef, TemplateInstance> entry : targetInstance.entrySet()) {
				entry.getKey().setResolvedSymbol(entry.getValue().getOrCreateNonterminal());
			}
		}
		if (terminals != null) {
			for (Entry<TemplatedSymbolRef, Terminal> entry : terminals.entrySet()) {
				entry.getKey().setResolvedSymbol(entry.getValue());
			}
		}
	}

	void allocate() {
		if (template.isTemplate()) {
			// TODO create and copy
			throw new UnsupportedOperationException("templates are not fully working yet");
		}
		updateExistingNonterminal();
	}
}
