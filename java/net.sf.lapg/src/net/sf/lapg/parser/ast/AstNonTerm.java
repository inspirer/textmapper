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

import java.util.List;
import java.util.Map;

import net.sf.lapg.parser.LapgTree.TextSource;

public class AstNonTerm extends AstNode implements AstGrammarPart {

	private final AstIdentifier name;
	private final String type;
	private final List<AstRule> rules;
	private final Map<String, Object> annotations;

	public AstNonTerm(AstIdentifier name, String type, List<AstRule> rules,
			Map<String, Object> annotations, TextSource source, int offset,
			int endoffset) {
		super(source, offset, endoffset);
		this.name = name;
		this.type = type;
		this.rules = rules;
		this.annotations = annotations;
	}

	public AstIdentifier getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public List<AstRule> getRules() {
		return rules;
	}

	public Map<String, Object> getAnnotations() {
		return annotations;
	}

	public void accept(AbstractVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if(name != null) {
			name.accept(v);
		}
		if(rules != null) {
			for(AstRule r : rules) {
				r.accept(v);
			}
		}
	}
}
