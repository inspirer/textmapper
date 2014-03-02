/**
 * Copyright 2002-2014 Evgeny Gryaznov
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

import java.util.List;
import org.textmapper.templates.types.TypesTree.TextSource;

public class AstConstraint extends AstNode {

	private final AstStringConstraint stringConstraint;
	private final List<AstMultiplicity> multiplicityListCommaSeparated;

	public AstConstraint(AstStringConstraint stringConstraint, List<AstMultiplicity> multiplicityListCommaSeparated, TextSource source, int line, int offset, int endoffset) {
		super(source, line, offset, endoffset);
		this.stringConstraint = stringConstraint;
		this.multiplicityListCommaSeparated = multiplicityListCommaSeparated;
	}

	public AstStringConstraint getStringConstraint() {
		return stringConstraint;
	}

	public List<AstMultiplicity> getMultiplicityListCommaSeparated() {
		return multiplicityListCommaSeparated;
	}

	public void accept(AstVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (stringConstraint != null) {
			stringConstraint.accept(v);
		}
		if (multiplicityListCommaSeparated != null) {
			for (AstMultiplicity it : multiplicityListCommaSeparated) {
				it.accept(v);
			}
		}
	}
}
