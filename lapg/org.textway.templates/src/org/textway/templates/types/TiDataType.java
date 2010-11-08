package org.textway.templates.types;

import java.util.Collection;

import org.textway.templates.api.types.IDataType;

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

	public static class TiConstraint implements Constraint {
		private ConstraintKind kind;
		private Collection<String> parameters;

		public TiConstraint(ConstraintKind kind, Collection<String> parameters) {
			this.kind = kind;
			this.parameters = parameters;
		}

		public ConstraintKind getKind() {
			return kind;
		}

		public Collection<String> getParameters() {
			return parameters;
		}
	}
}
