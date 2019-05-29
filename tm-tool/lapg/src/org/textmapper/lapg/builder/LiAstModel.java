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
package org.textmapper.lapg.builder;

import org.textmapper.lapg.api.ast.AstClassifier;
import org.textmapper.lapg.api.ast.AstModel;

class LiAstModel implements AstModel {

	private final AstClassifier[] classifiers;

	LiAstModel(AstClassifier[] classifiers) {
		this.classifiers = classifiers;
	}

	@Override
	public AstClassifier[] getClassifiers() {
		return classifiers;
	}
}
