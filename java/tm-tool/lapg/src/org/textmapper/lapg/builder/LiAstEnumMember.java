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
package org.textmapper.lapg.builder;

import org.textmapper.lapg.api.DerivedSourceElement;
import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.ast.AstEnum;
import org.textmapper.lapg.api.ast.AstEnumMember;

class LiAstEnumMember extends LiUserDataHolder implements AstEnumMember, DerivedSourceElement {

	private final String name;
	private final AstEnum container;
	private final SourceElement origin;

	public LiAstEnumMember(String name, AstEnum container, SourceElement origin) {
		this.name = name;
		this.container = container;
		this.origin = origin;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public AstEnum getContainingEnum() {
		return container;
	}

	@Override
	public SourceElement getOrigin() {
		return origin;
	}
}
