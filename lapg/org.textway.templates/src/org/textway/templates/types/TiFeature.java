/**
 * Copyright 2002-2010 Evgeny Gryaznov
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

	public String getName() {
		return name;
	}

	public IType getType() {
		return type;
	}

	public void setType(IType type) {
		this.type = type;
	}

	public IMultiplicity getMultiplicity() {
		return multiplicity;
	}

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

		public int getLowBound() {
			return loBound;
		}

		public boolean isMultiple() {
			return hiBound == -1 || hiBound > 1;
		}

		public int getHighBound() {
			return hiBound;
		}
	}
}
