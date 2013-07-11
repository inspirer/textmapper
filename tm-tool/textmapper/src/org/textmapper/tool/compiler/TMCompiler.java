/**
 * Copyright 2002-2013 Evgeny Gryaznov
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
import org.textmapper.lapg.api.Grammar;
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.TextSourceElement;
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
		TMExpressionResolver expressionResolver = new TMExpressionResolver(resolver, types, getTypesPackage());

		Map<String, Object> options = getOptions(resolver, expressionResolver);

		new TMLexerCompiler(resolver).compile();

		if (tree.getRoot().getGrammar() != null) {
			new TMParserCompiler(resolver, expressionResolver).compile();
		}

		TextSourceElement templates = getTemplates();
		String copyrightHeader = TMTextUtil.extractCopyright(tree.getSource());

		Grammar g = builder.create();
		generateUniqueIds(g);
		return new TMGrammar(g, templates, !tree.getErrors().isEmpty(), options, copyrightHeader);
	}

	private void generateUniqueIds(Grammar g) {
		UniqueNameHelper helper = new UniqueNameHelper();
		for (Symbol s : g.getSymbols()) {
			String name = s.getName();
			if (FormatUtil.isIdentifier(name)) {
				helper.markUsed(name);
			}
		}
		for (int i = 0; i < g.getSymbols().length; i++) {
			Symbol sym = g.getSymbols()[i];
			TMDataUtil.putId(sym, helper.generateSymbolId(sym.getName(), i));
		}
	}

	private TextSourceElement getTemplates() {
		int offset = tree.getRoot() != null ? tree.getRoot().getTemplatesStart() : -1;
		char[] text = tree.getSource().getContents();
		if (offset < text.length && offset != -1) {
			return new TmaNode(tree.getSource(), offset, text.length) {
				@Override
				public void accept(TmaVisitor v) {
				}
			};
		}
		return null;
	}

	private String getTypesPackage() {
		if (tree.getRoot().getOptions() != null) {
			for (TmaOptionPart option : tree.getRoot().getOptions()) {
				if (option instanceof TmaOption && ((TmaOption) option).getKey().equals("lang")) {
					ITmaExpression expression = ((TmaOption) option).getValue();
					if (expression instanceof TmaExpressionLiteral) {
						return ((TmaExpressionLiteral) expression).getLiteral().toString();
					}
				}
			}
		}

		return "common";
	}

	private Map<String, Object> getOptions(TMResolver resolver, TMExpressionResolver expressionResolver) {
		Map<String, Object> options = new HashMap<String, Object>();

		// Load class
		IClass optionsClass = types.getClass(expressionResolver.getTypesPackage() + ".Options", null);
		if (optionsClass == null) {
			resolver.error(tree.getRoot(), "cannot load options class `" + expressionResolver.getTypesPackage() + ".Options`");
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
		for (TmaOptionPart option : tree.getRoot().getOptions()) {
			if (option instanceof TmaOption) {
				String key = ((TmaOption) option).getKey();
				IFeature feature = optionsClass.getFeature(key);
				if (feature == null) {
					resolver.error(option, "unknown option `" + key + "`");
					continue;
				}

				ITmaExpression value = ((TmaOption) option).getValue();
				options.put(key, expressionResolver.convertExpression(value, feature.getType()));
			}
		}
		return options;
	}

}
