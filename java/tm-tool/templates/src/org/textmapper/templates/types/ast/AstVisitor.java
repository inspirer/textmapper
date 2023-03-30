/**
 * Copyright 2002-2022 Evgeny Gryaznov
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
package org.textmapper.templates.types.ast;

public abstract class AstVisitor {

	protected boolean visit(AstInput n) {
		return true;
	}

	protected boolean visit(AstTypeDeclaration n) {
		return true;
	}

	protected boolean visit(AstFeatureDeclaration n) {
		return true;
	}

	protected boolean visit(AstMethodDeclaration n) {
		return true;
	}

	protected boolean visit(AstConstraint n) {
		return true;
	}

	protected boolean visit(AstStringConstraint n) {
		return true;
	}

	protected boolean visit(Ast_String n) {
		return true;
	}

	protected boolean visit(AstMultiplicity n) {
		return true;
	}

	protected boolean visit(AstTypeEx n) {
		return true;
	}

	protected boolean visit(AstType n) {
		return true;
	}

	protected boolean visit(AstLiteralExpression n) {
		return true;
	}

	protected boolean visit(AstStructuralExpression n) {
		return true;
	}

	protected boolean visit(AstListOfIDENTIFIERAnd2ElementsCommaSeparatedItem n) {
		return true;
	}
}
