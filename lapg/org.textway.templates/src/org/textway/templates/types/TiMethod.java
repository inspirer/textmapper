/**
 * Copyright 2002-2010 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.textway.templates.types;

import org.textway.templates.api.types.IMethod;
import org.textway.templates.api.types.IType;

public class TiMethod implements IMethod {

	private final String name;
	private final IType returnType;
	private final IType[] parameterTypes;

	public TiMethod(String name, IType returnType, IType[] parameterTypes) {
		this.name = name;
		this.returnType = returnType;
		this.parameterTypes = parameterTypes;
	}

	public String getName() {
		return name;
	}

	public IType getReturnType() {
		return returnType;
	}

	public IType[] getParameterTypes() {
		return parameterTypes;
	}
}
