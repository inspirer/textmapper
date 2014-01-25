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

public class TmaVisitor {

	public boolean visit(TmaCommand n) {
		return true;
	}

	public boolean visit(TmaDirectivePrio n) {
		return true;
	}

	public boolean visit(TmaDirectiveInput n) {
		return true;
	}

	public boolean visit(TmaStateSelector n) {
		return true;
	}

	public boolean visit(TmaIdentifier n) {
		return true;
	}

	public boolean visit(TmaNamedPattern n) {
		return true;
	}

	public boolean visit(TmaLexeme n) {
		return true;
	}

	public boolean visit(TmaNonterm n) {
		return true;
	}

	public boolean visit(TmaOption n) {
		return true;
	}

	public boolean visit(TmaSymref astSymbol) {
		return true;
	}

	public boolean visit(TmaPattern n) {
		return true;
	}

	public boolean visit(TmaInput n) {
		return true;
	}

	public boolean visit(TmaRule0 n) {
		return true;
	}

	public boolean visit(TmaSyntaxProblem n) {
		return true;
	}

	public boolean visit(TmaAnnotations n) {
		return true;
	}

	public boolean visit(TmaArray n) {
		return true;
	}

	public boolean visit(TmaInstance n) {
		return true;
	}

	public boolean visit(TmaMapEntriesItem n) {
		return true;
	}

	public boolean visit(TmaLiteral n) {
		return true;
	}

	public boolean visit(TmaName n) {
		return true;
	}

	public boolean visit(TmaRhsSuffix n) {
		return true;
	}

	public boolean visit(TmaLexemAttrs n) {
		return true;
	}

	public boolean visit(TmaInputref n) {
		return true;
	}

	public boolean visit(TmaNegativeLa n) {
		return true;
	}

	public boolean visit(TmaRhsSymbol n) {
		return true;
	}

	public boolean visit(TmaRhsNested n) {
		return true;
	}

	public boolean visit(TmaRhsQuantifier n) {
		return true;
	}

	public boolean visit(TmaRhsUnordered n) {
		return true;
	}

	public boolean visit(TmaRhsList n) {
		return true;
	}

	public boolean visit(TmaLexerState n) {
		return true;
	}

	public boolean visit(TmaRhsCast n) {
		return true;
	}

	public boolean visit(TmaRhsAssignment n) {
		return true;
	}

	public boolean visit(TmaRhsAnnotated n) {
		return true;
	}

	public boolean visit(TmaRhsPrefix n) {
		return true;
	}

	public boolean visit(TmaNontermTypeAST n) {
		return true;
	}

	public boolean visit(TmaNontermTypeHint n) {
		return true;
	}

	public boolean visit(TmaNontermTypeRaw n) {
		return true;
	}

	public boolean visit(TmaStateref n) {
		return true;
	}

	public boolean visit(TmaImport n) {
		return true;
	}

	public boolean visit(TmaHeader n) {
		return true;
	}

	public boolean visit(TmaRhsAsLiteral n) {
		return true;
	}

	public boolean visit(TmaAnnotation n) {
		return true;
	}

	public boolean visit(TmaRhsClass n) {
		return true;
	}

	public boolean visit(TmaParsingAlgorithm n) {
		return true;
	}

	public boolean visit(TmaRhsAnnotations n) {
		return true;
	}
}
