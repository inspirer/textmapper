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

public class AstMethodDeclaration extends AstNode implements IAstMemberDeclaration {

	private AstTypeEx returnType;
	private String name;
	private List<AstTypeEx> parametersopt;

	public AstMethodDeclaration(AstTypeEx returnType, String name, List<AstTypeEx> parametersopt, TextSource input, int start, int end) {
		super(input, start, end);
		this.returnType = returnType;
		this.name = name;
		this.parametersopt = parametersopt;
	}

	public AstTypeEx getReturnType() {
		return returnType;
	}
	public String getName() {
		return name;
	}
	public List<AstTypeEx> getParametersopt() {
		return parametersopt;
	}
	public void accept(AstVisitor v) {
		if (!v.visit(this)) {
			return;
		}

		if (returnType != null) {
			returnType.accept(v);
		}
		// TODO for name
		if (parametersopt != null) {
			for (AstTypeEx it : parametersopt) {
				it.accept(v);
			}
		}
	}
}
