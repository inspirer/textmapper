/**
 * Copyright 2002-2019 Evgeny Gryaznov
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
import org.textmapper.templates.api.types.IType;

import java.util.Collection;

public class TiDataType implements IDataType {

	private DataTypeKind kind;
	private Collection<Constraint> constraints;

	public TiDataType(DataTypeKind kind, Collection<Constraint> constraints) {
		this.kind = kind;
		this.constraints = constraints;
	}

	@Override
	public DataTypeKind getKind() {
		return kind;
	}

	@Override
	public Collection<Constraint> getConstraints() {
		return constraints;
	}

	@Override
	public boolean isSubtypeOf(IType anotherType) {
		if(!(anotherType instanceof IDataType)) {
			return false;
		}
		IDataType another = (IDataType) anotherType;
		if(kind != another.getKind()) {
			return false;
		}
		// TODO compare constraints
		return true;
	}

	@Override
	public String toString() {
		switch(kind) {
			case BOOL:
				return "bool";
			case INT:
				return "int";
			case STRING:
				return "string";
		}
		return "unknown";
	}

	public static class TiConstraint implements Constraint {
		private ConstraintKind kind;
		private Collection<String> parameters;

		public TiConstraint(ConstraintKind kind, Collection<String> parameters) {
			this.kind = kind;
			this.parameters = parameters;
		}

		@Override
		public ConstraintKind getKind() {
			return kind;
		}

		@Override
		public Collection<String> getParameters() {
			return parameters;
		}
	}
}
