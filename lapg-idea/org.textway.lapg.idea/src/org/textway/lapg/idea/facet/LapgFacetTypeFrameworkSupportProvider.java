/**
 * Copyright 2002-2011 Evgeny Gryaznov
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
package org.textway.lapg.idea.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetTypeRegistry;
import com.intellij.facet.ui.FacetBasedFrameworkSupportProvider;
import com.intellij.ide.util.frameworkSupport.FrameworkVersion;
import com.intellij.openapi.roots.ModifiableRootModel;

public class LapgFacetTypeFrameworkSupportProvider extends FacetBasedFrameworkSupportProvider {

	protected LapgFacetTypeFrameworkSupportProvider() {
		super(FacetTypeRegistry.getInstance().findFacetType(LapgFacetType.ID));
	}

	@Override
	protected void setupConfiguration(Facet facet, ModifiableRootModel rootModel, FrameworkVersion version) {
		// do nothing
	}
}
