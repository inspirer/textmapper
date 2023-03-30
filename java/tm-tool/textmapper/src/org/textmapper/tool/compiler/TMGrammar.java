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
package org.textmapper.tool.compiler;

import org.textmapper.lapg.api.Grammar;
import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.TextSourceElement;
import org.textmapper.lapg.api.UserDataHolder;

import java.util.Map;

public class TMGrammar {

	private final Grammar grammar;
	private final TextSourceElement templates;
	private final boolean hasErrors;
	private final Map<String, Object> options;
	private final String copyrightHeader;
	private final String targetLanguage;

	public TMGrammar(Grammar grammar, TextSourceElement templates, boolean hasErrors,
					 Map<String, Object> options, String copyrightHeader, String targetLanguage) {
		this.grammar = grammar;
		this.templates = templates;
		this.hasErrors = hasErrors;
		this.options = options;
		this.copyrightHeader = copyrightHeader;
		this.targetLanguage = targetLanguage;
	}

	public Grammar getGrammar() {
		return grammar;
	}

	public TextSourceElement getTemplates() {
		return templates;
	}

	public boolean hasErrors() {
		return hasErrors;
	}

	public Map<String, Object> getOptions() {
		return options;
	}

	public String getCopyrightHeader() {
		return copyrightHeader;
	}

	public String getTargetLanguage() {
		return targetLanguage;
	}

	public Object getAnnotation(SourceElement element, String name) {
		Map<String, Object> annotations = TMDataUtil.getAnnotations((UserDataHolder) element);
		return annotations != null ? annotations.get(name) : null;
	}

}
