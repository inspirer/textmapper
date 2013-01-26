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
package org.textmapper.lapg.builder;

import org.textmapper.lapg.api.DerivedSourceElement;
import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.ast.AstClass;
import org.textmapper.lapg.api.ast.AstEnum;
import org.textmapper.lapg.api.ast.AstModel;

class LiAstModel implements AstModel, DerivedSourceElement {

	private final AstClass[] classes;
	private final AstEnum[] enums;
	private final SourceElement origin;

	LiAstModel(AstClass[] classes, AstEnum[] enums, SourceElement origin) {
		this.classes = classes;
		this.enums = enums;
		this.origin = origin;
	}

	@Override
	public AstClass[] getClasses() {
		return classes;
	}

	@Override
	public AstEnum[] getEnums() {
		return enums;
	}

	@Override
	public SourceElement getOrigin() {
		return origin;
	}
}
