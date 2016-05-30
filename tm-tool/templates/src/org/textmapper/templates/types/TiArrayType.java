/**
 * Copyright 2002-2016 Evgeny Gryaznov
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
package org.textmapper.templates.types;

import org.textmapper.templates.api.types.IArrayType;
import org.textmapper.templates.api.types.IType;

public class TiArrayType implements IArrayType {

	private final IType innerType;

	public TiArrayType(IType innerType) {
		this.innerType = innerType;
	}

	@Override
	public IType getInnerType() {
		return innerType;
	}

	@Override
	public boolean isSubtypeOf(IType anotherType) {
		if(anotherType instanceof IArrayType) {
			return innerType.isSubtypeOf(((IArrayType) anotherType).getInnerType());
		}
		return false;
	}

	@Override
	public String toString() {
		return innerType.toString() + "[]";
	}
}
