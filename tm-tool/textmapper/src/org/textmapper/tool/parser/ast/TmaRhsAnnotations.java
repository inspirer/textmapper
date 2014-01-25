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
package org.textmapper.tool.parser.ast;

import org.textmapper.tool.parser.TMTree.TextSource;

import java.util.List;

/**
 * Gryaznov Evgeny, 8/15/11
 */
public class TmaRhsAnnotations extends TmaNode {

	private final TmaNegativeLa negativeLA;
	private final List<TmaAnnotation> annotations;

	public TmaRhsAnnotations(TmaNegativeLa negativeLA, List<TmaAnnotation> annotations, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.annotations = annotations;
		this.negativeLA = negativeLA;
	}

	public TmaNegativeLa getNegativeLA() {
		return negativeLA;
	}

	public List<TmaAnnotation> getAnnotations() {
		return annotations;
	}

	public void accept(TmaVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (negativeLA != null) {
			negativeLA.accept(v);
		}
		if (annotations != null) {
			for (TmaAnnotation it : annotations) {
				it.accept(v);
			}
		}
	}
}
