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
package org.textway.templates.bundle;

import org.textway.templates.api.IProblemCollector;
import org.textway.templates.ast.TemplatesTree;
import org.textway.templates.ast.TemplatesTree.TemplatesProblem;
import org.textway.templates.ast.TemplatesTree.TextSource;

import java.util.List;

public class TemplatesBundle {

	private final String sourceName;
	private final IBundleEntity[] entities;

	public TemplatesBundle(String sourceName, IBundleEntity[] entities) {
		this.sourceName = sourceName;
		this.entities = entities;
	}

	public String getName() {
		return sourceName;
	}

	public IBundleEntity[] getEntities() {
		return entities;
	}

	public static TemplatesBundle parse(final String sourceName, String contents, String templatePackage,
										final IProblemCollector collector) {

		TemplatesTree<List<IBundleEntity>> tree = TemplatesTree.parseInput(new TextSource(sourceName, contents.toCharArray(), 1), templatePackage);
		for (TemplatesProblem problem : tree.getErrors()) {
			final int line = tree.getSource().lineForOffset(problem.getOffset());
			collector.fireError(new ILocatedEntity() {
				public String getLocation() {
					return sourceName + "," + line;
				}
			}, problem.getMessage());
		}
		IBundleEntity[] entities = tree.getRoot() != null ? tree.getRoot().toArray(new IBundleEntity[tree.getRoot().size()]) : new IBundleEntity[0];
		return new TemplatesBundle(sourceName, entities);
	}
}
