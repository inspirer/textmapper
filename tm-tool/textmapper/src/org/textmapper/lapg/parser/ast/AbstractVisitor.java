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

public class AbstractVisitor {

	public boolean visit(AstCode n) {
		return true;
	}

	public boolean visit(AstDirective n) {
		return true;
	}

	public boolean visit(AstInputDirective n) {
		return true;
	}

	public boolean visit(AstGroupsSelector n) {
		return true;
	}

	public boolean visit(AstIdentifier n) {
		return true;
	}

	public boolean visit(AstNamedPattern n) {
		return true;
	}

	public boolean visit(AstLexeme n) {
		return true;
	}

	public boolean visit(AstNonTerm n) {
		return true;
	}

	public boolean visit(AstOption n) {
		return true;
	}

	public boolean visit(AstReference astSymbol) {
		return true;
	}

	public boolean visit(AstRegexp n) {
		return true;
	}

	public boolean visit(AstRoot n) {
		return true;
	}

	public boolean visit(AstRule n) {
		return true;
	}

	public boolean visit(AstRefRulePart n) {
		return true;
	}

	public boolean visit(AstError n) {
		return true;
	}

	public boolean visit(AstAnnotations n) {
		return true;
	}

	public boolean visit(AstArray n) {
		return true;
	}

	public boolean visit(AstInstance n) {
		return true;
	}

	public boolean visit(AstNamedEntry n) {
		return true;
	}

	public boolean visit(AstLiteralExpression n) {
		return true;
	}

	public boolean visit(AstName n) {
		return true;
	}

	public boolean visit(AstPrioClause n) {
		return true;
	}

	public boolean visit(AstShiftClause n) {
		return true;
	}

	public boolean visit(AstLexemAttrs n) {
		return true;
	}

	public boolean visit(AstInputRef n) {
		return true;
	}

	public boolean visit(AstNegativeLA n) {
		return true;
	}

	public boolean visit(AstRuleDefaultSymbolRef n) {
		return true;
	}

	public boolean visit(AstRuleNestedNonTerm n) {
		return true;
	}

	public boolean visit(AstRuleNestedQuantifier n) {
		return true;
	}

	public boolean visit(AstUnorderedRulePart n) {
		return true;
	}
}
