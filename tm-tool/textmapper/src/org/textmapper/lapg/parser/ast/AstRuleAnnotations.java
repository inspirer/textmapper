/**
 * Copyright 2002-2012 Evgeny Gryaznov
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
package org.textmapper.lapg.parser.ast;

import org.textmapper.lapg.parser.LapgTree.TextSource;

import java.util.List;

/**
 * Gryaznov Evgeny, 8/15/11
 */
public class AstRuleAnnotations extends AstAnnotations {

	private final AstNegativeLA negativeLA;

	public AstRuleAnnotations(AstNegativeLA negativeLA, List<AstNamedEntry> annotations, TextSource source, int offset, int endoffset) {
		super(annotations, source, offset, endoffset);
		this.negativeLA = negativeLA;
	}

	public AstNegativeLA getNegativeLA() {
		return negativeLA;
	}

	public void accept(AbstractVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (negativeLA != null) {
			negativeLA.accept(v);
		}
		if (getAnnotations() != null) {
			for (AstNamedEntry n : getAnnotations()) {
				n.accept(v);
			}
		}
	}
}
