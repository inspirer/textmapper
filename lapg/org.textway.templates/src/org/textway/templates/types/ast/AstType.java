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
package org.textway.templates.types.ast;

import java.util.List;
import org.textway.templates.types.TypesTree.TextSource;

public class AstType extends AstNode {

	public static final int LINT = 1;
	public static final int LSTRING = 2;
	public static final int LBOOL = 3;

	private int kind;
	private boolean isReference;
	private boolean isClosure;
	private List<String> name;
	private List<AstTypeEx> parametersopt;

	public AstType(int kind, boolean isReference, boolean isClosure, List<String> name, List<AstTypeEx> parametersopt, TextSource input, int start, int end) {
		super(input, start, end);
		this.kind = kind;
		this.isReference = isReference;
		this.isClosure = isClosure;
		this.name = name;
		this.parametersopt = parametersopt;
	}

	public int getKind() {
		return kind;
	}
	public boolean getIsReference() {
		return isReference;
	}
	public boolean getIsClosure() {
		return isClosure;
	}
	public List<String> getName() {
		return name;
	}
	public List<AstTypeEx> getParametersopt() {
		return parametersopt;
	}
	public void accept(AstVisitor v) {
		if (!v.visit(this)) {
			return;
		}

		// TODO for name
		if (parametersopt != null) {
			for (AstTypeEx it : parametersopt) {
				it.accept(v);
			}
		}
	}
}
