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

import java.util.Collection;

import org.textway.templates.api.types.IClass;
import org.textway.templates.api.types.IFeature;

public class TiClass implements IClass {

	private String name;
	private Collection<IClass> _super;
	private Collection<IFeature> features;
	
	public TiClass(String name, Collection<IClass> _super,
			Collection<IFeature> features) {
		this.name = name;
		this._super = _super;
		this.features = features;
	}

	public String getName() {
		return name;
	}

	public Collection<IClass> getExtends() {
		return _super;
	}

	public Collection<IFeature> getFeatures() {
		return features;
	}
}
