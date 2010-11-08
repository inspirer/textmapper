package org.textway.templates.types;

import org.textway.templates.api.types.IFeature;
import org.textway.templates.api.types.IMultiplicity;
import org.textway.templates.api.types.IType;

public class TiFeature implements IFeature {

	private String name;
	private IType type;
	private IMultiplicity multiplicity;
	private boolean isReference;

	public TiFeature(String name, int loBound, int hiBound, boolean isReference) {
		this.name = name;
		this.multiplicity = new TiMultiplicity(loBound, hiBound);
		this.isReference = isReference;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public IType getType() {
		return type;
	}

	public void setType(IType type) {
		this.type = type;
	}

	@Override
	public IMultiplicity getMultiplicity() {
		return multiplicity;
	}

	@Override
	public boolean isReference() {
		return isReference;
	}

	private final static class TiMultiplicity implements IMultiplicity {

		private int loBound;
		private int hiBound;

		public TiMultiplicity(int loBound, int hiBound) {
			this.loBound = loBound;
			this.hiBound = hiBound;
		}

		@Override
		public int getLowBound() {
			return loBound;
		}

		@Override
		public int getHighBound() {
			return hiBound;
		}
	}
}
