package org.textway.templates.api.types;

import java.util.Collection;

public interface IDataType extends IType {

	DataTypeKind getKind();

	Collection<Constraint> getConstraints();

	public enum DataTypeKind {
		STRING, BOOL, INT
	}

	public enum ConstraintKind {
		NOTEMPTY, IDENTIFIER, QUALIFIED_IDENTIFIER, SET, CHOICE
	}

	public interface Constraint {
		ConstraintKind getKind();

		Collection<String> getParameters();
	}
}
