/**
 * Copyright 2002-2015 Evgeny Gryaznov
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

import org.textmapper.templates.api.types.IClosureType;
import org.textmapper.templates.api.types.IType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Gryaznov Evgeny, 3/12/11
 */
public class TiClosureType implements IClosureType {

	private IType[] parameterTypes;

	public TiClosureType(IType ...parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	@Override
	public boolean isSubtypeOf(IType anotherType) {
		if(!(anotherType instanceof IClosureType)) {
			return false;
		}
		IClosureType another = (IClosureType) anotherType;
		Collection<IType> anotherTypes = another.getParameterTypes();
		int paramsLength = parameterTypes == null ? 0 : parameterTypes.length;
		if(paramsLength != anotherTypes.size()) {
			return false;
		}
		int i = 0;
		for(IType their : anotherTypes) {
			if(!their.isSubtypeOf(parameterTypes[i++])) {
				return false;
			}
		}
		return true;
	}

	@Override
	public Collection<IType> getParameterTypes() {
		return parameterTypes == null ? Collections.<IType>emptyList() : Arrays.asList(parameterTypes);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{ ");
		if(parameterTypes != null) {
			for(int i = 0; i < parameterTypes.length; i++) {
				if(i > 0) {
					sb.append(", ");
				}
				sb.append(parameterTypes[i].toString());
			}
			sb.append(" ");
		}
		sb.append("=> }");
		return sb.toString();
	}
}
