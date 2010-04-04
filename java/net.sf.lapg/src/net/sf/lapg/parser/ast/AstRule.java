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

public class AstRule extends AstNode {

	private final List<AstRuleSymbol> list;
	private final AstCode action;
	private final AstReference priority;
	private final Map<String,Object> annotations;

	public AstRule(List<AstRuleSymbol> list, AstCode action, AstReference priority, Map<String,Object> annotations, TextSource source,
			int offset, int endoffset) {
		super(source, offset, endoffset);
		this.list = list;
		this.action = action;
		this.priority = priority;
		this.annotations = annotations;
	}

	public List<AstRuleSymbol> getList() {
		return list;
	}

	public AstCode getAction() {
		return action;
	}

	public AstReference getPriority() {
		return priority;
	}

	public Map<String, Object> getAnnotations() {
		return annotations;
	}

	public void accept(AbstractVisitor v) {
		if(!v.visit(this)) {
			return;
		}
		if(list != null) {
			for(AstRuleSymbol rsym : list) {
				rsym.accept(v);
			}
		}
		if(action != null) {
			action.accept(v);
		}
		if(priority != null) {
			priority.accept(v);
		}
	}
}
