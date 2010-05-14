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
package net.sf.lapg.templates.api;

import net.sf.lapg.templates.ast.AstParser;
import net.sf.lapg.templates.ast.AstLexer.ErrorReporter;
import net.sf.lapg.templates.ast.AstTree.TextSource;

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
		TextSource source = new TextSource(sourceName, contents.toCharArray(), 1);
		AstParser p = new AstParser(new ErrorReporter() {
			public void error(int start, int end, int line, String s) {
				collector.fireError(null, sourceName + ":" + s);
			}
		});
		IBundleEntity[] entities;
		if (!p.parse(source, templatePackage)) {
			entities = new IBundleEntity[0];
		} else {
			entities = p.getResult();
		}
		return new TemplatesBundle(sourceName, entities);
	}
}
