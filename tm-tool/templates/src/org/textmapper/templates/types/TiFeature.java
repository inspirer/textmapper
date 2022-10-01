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
package org.textmapper.templates.types;

import org.textmapper.templates.api.types.IFeature;
import org.textmapper.templates.api.types.IMultiplicity;
import org.textmapper.templates.api.types.IType;

public class TiFeature implements IFeature {

	private String name;
	private IType type;
	private IMultiplicity[] multiplicities;
	private boolean isReference;
	private Object defaultValue;

	public TiFeature(String name, boolean isReference, IMultiplicity... multiplicities) {
		this.name = name;
		this.multiplicities = multiplicities;
		this.isReference = isReference;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public IType getTarget() {
		return type;
	}

	@Override
	public IType getType() {
		IType type = getTarget();
		if (multiplicities != null) {
			for (int i = multiplicities.length - 1; i >= 0; i--) {
				if (multiplicities[i].isMultiple()) {
					type = new TiArrayType(type);
				}
			}
		}
		return type;
	}

	public void setType(IType type) {
		this.type = type;
	}

	@Override
	public IMultiplicity[] getMultiplicities() {
		return multiplicities;
	}

	@Override
	public boolean isReference() {
		return isReference;
	}

	@Override
	public Object getDefaultValue() {
		return defaultValue;
	}

	void setDefaultValue(Object defaultValue) {
		this.defaultValue = defaultValue;
	}

	public final static class TiMultiplicity implements IMultiplicity {

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
		public boolean isMultiple() {
			return hiBound == -1 || hiBound > 1;
		}

		@Override
		public int getHighBound() {
			return hiBound;
		}
	}
}
