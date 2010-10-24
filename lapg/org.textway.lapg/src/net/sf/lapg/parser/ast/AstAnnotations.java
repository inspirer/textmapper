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

import net.sf.lapg.parser.LapgTree.TextSource;

import java.util.List;

public class AstAnnotations extends AstNode {

	private final List<AstNamedEntry> annotations;

	public AstAnnotations(List<AstNamedEntry> annotations, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.annotations = annotations;
	}

	public List<AstNamedEntry> getAnnotations() {
		return annotations;
	}

	public void accept(AbstractVisitor v) {
		if(!v.visit(this)) {
			return;
		}
		if(annotations != null) {
			for(AstNamedEntry n : annotations) {
				n.accept(v);
			}
		}
	}
}
