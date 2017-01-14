/**
 * Copyright 2002-2017 Evgeny Gryaznov
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

import org.textmapper.lapg.LapgCore;
import org.textmapper.lapg.api.*;
import org.textmapper.lapg.api.builder.GrammarBuilder;
import org.textmapper.lapg.common.FormatUtil;
import org.textmapper.templates.api.types.IClass;
import org.textmapper.templates.api.types.IFeature;
import org.textmapper.templates.types.TypesRegistry;
import org.textmapper.tool.parser.TMTree;
import org.textmapper.tool.parser.ast.*;

import java.util.HashMap;
import java.util.Map;

public class TMCompiler {

	private final TMTree<TmaInput> tree;
	private final TypesRegistry types;

	public TMCompiler(TMTree<TmaInput> tree, TypesRegistry types) {
		this.tree = tree;
		this.types = types;
	}

	public TMGrammar resolve() {
		if (tree.getRoot() == null) {
			return null;
		}

		GrammarBuilder builder = LapgCore.createBuilder();
		TMResolver resolver = new TMResolver(tree, builder);
		resolver.collectSymbols();

		String targetLanguage = getTargetLanguage();
		TMExpressionResolver expressionResolver = new TMExpressionResolver(
				resolver, types, targetLanguage);

		Map<String, Object> options = getOptions(resolver, expressionResolver);

		new TMLexerCompiler(resolver).compile();

		if (tree.getRoot().getParser() != null) {
			new TMParserCompiler(resolver, expressionResolver).compile();
		}

		TextSourceElement templates = getTemplates();
		String copyrightHeader = TMTextUtil.extractCopyright(tree.getSource());

		Grammar g = builder.create();
		for (Problem p : g.getProblems()) {
			resolver.error(unwrap(p.getSourceElement()), p.getMessage());
		}
		generateUniqueIds(g, options.get("tokensAllCaps") == Boolean.TRUE);
		return new TMGrammar(g, templates, !tree.getErrors().isEmpty(), options,
				copyrightHeader, targetLanguage);
	}

	private void generateUniqueIds(Grammar g, boolean tokensAllCaps) {
		UniqueNameHelper helper = new UniqueNameHelper(tokensAllCaps);
		for (Symbol s : g.getSymbols()) {
			helper.add(s);
		}
		helper.apply();
	}

	private TextSourceElement getTemplates() {
		int offset = tree.getRoot() != null ? tree.getRoot().getTemplatesStart() : -1;
		CharSequence text = tree.getSource().getContents();
		if (offset < text.length() && offset != -1) {
			return new TmaNode(tree.getSource(), tree.getSource().lineForOffset(offset),
					offset, text.length()) {
				@Override
				public void accept(TmaVisitor v) {
				}
			};
		}
		return null;
	}

	private String getTargetLanguage() {
		final TmaName targetLanguage = tree.getRoot().getHeader().getTarget();
		if (targetLanguage != null) {
			return targetLanguage.getQualifiedId();
		}
		return "common";
	}

	private Map<String, Object> getOptions(TMResolver resolver,
										   TMExpressionResolver expressionResolver) {
		Map<String, Object> options = new HashMap<>();

		// Load class
		IClass optionsClass = types.getClass(
				expressionResolver.getTypesPackage() + ".Options", null);
		if (optionsClass == null) {
			resolver.error(tree.getRoot(), "cannot load options class `"
					+ expressionResolver.getTypesPackage() + ".Options`");
			return options;
		}

		// fill default values
		for (IFeature feature : optionsClass.getFeatures()) {
			Object value = feature.getDefaultValue();
			if (value != null) {
				options.put(feature.getName(), value);
			}
		}

		// overrides
		if (tree.getRoot().getOptions() == null) {
			return options;
		}
		for (TmaOption option : tree.getRoot().getOptions()) {
			if (option.getSyntaxProblem() != null) {
				continue;
			}

			String key = option.getKey();
			IFeature feature = optionsClass.getFeature(key);
			if (feature == null) {
				resolver.error(option, "unknown option `" + key + "`");
				continue;
			}

			ITmaExpression value = option.getValue();
			options.put(key, expressionResolver.convertExpression(value, feature.getType()));
		}
		return options;
	}

	private TextSourceElement unwrap(SourceElement element) {
		while (element instanceof DerivedSourceElement) {
			element = ((DerivedSourceElement) element).getOrigin();
		}
		if (element instanceof TextSourceElement) {
			return (TextSourceElement) element;
		}
		return null;

	}
}
