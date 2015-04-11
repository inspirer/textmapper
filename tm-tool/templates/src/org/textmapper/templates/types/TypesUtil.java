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

import org.textmapper.templates.api.types.IDataType;
import org.textmapper.templates.api.types.IDataType.DataTypeKind;
import org.textmapper.templates.api.types.IFeature;
import org.textmapper.templates.api.types.IType;

public class TypesUtil {

	public static boolean isBooleanType(IType type) {
		return type instanceof IDataType && ((IDataType) type).getKind() == DataTypeKind.BOOL;
	}
}
