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
package org.textway.lapg.gen;

import org.textway.lapg.api.Grammar;
import org.textway.lapg.api.ProcessingStatus;
import org.textway.lapg.api.ProcessingStrategy;
import org.textway.lapg.api.SourceElement;
import org.textway.lapg.lalr.Builder;
import org.textway.lapg.lalr.ParserTables;
import org.textway.lapg.lex.LexerTables;
import org.textway.lapg.lex.LexicalBuilder;
import org.textway.lapg.parser.LapgTree.TextSource;
import org.textway.templates.api.EvaluationContext;
import org.textway.templates.api.TemplatesStatus;
import org.textway.templates.api.types.IClass;
import org.textway.templates.bundle.DefaultTemplateLoader;
import org.textway.templates.bundle.IBundleLoader;
import org.textway.templates.bundle.StringTemplateLoader;
import org.textway.templates.bundle.TemplatesRegistry;
import org.textway.templates.eval.TemplatesFacade;
import org.textway.templates.objects.IxFactory;
import org.textway.templates.storage.ClassResourceLoader;
import org.textway.templates.storage.IResourceLoader;
import org.textway.templates.storage.Resource;
import org.textway.templates.storage.ResourceRegistry;
import org.textway.templates.types.TiInstance;
import org.textway.templates.types.TypesRegistry;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public final class LapgGenerator {

	private final LapgOptions options;
	private final ProcessingStatus status;
	private final ProcessingStrategy strategy;

	public LapgGenerator(LapgOptions options, ProcessingStatus status, ProcessingStrategy strategy) {
		this.options = options;
		this.status = status;
		this.strategy = strategy;
	}

	public boolean compileGrammar(TextSource input) {
		try {
			TemplatesStatusAdapter templatesStatus = new TemplatesStatusAdapter(status);
			ResourceRegistry resources = createResourceRegistry();
			TypesRegistry types = new TypesRegistry(resources, templatesStatus);

			Grammar s = SyntaxUtil.parseSyntax(input, status, types);
			if (s == null || s.hasErrors()) {
				return false;
			}

			TemplatesRegistry registry = createTemplateRegistry(s.getTemplates(), resources, types, templatesStatus);
			if (!checkOptions(s, registry)) {
				return false;
			}

			// prepare options
			Map<String, Object> genOptions = new HashMap<String, Object>(s.getOptions());
			for (Entry<String,String> entry : options.getAdditionalOptions().entrySet()) {

				// TODO parse value, check type
				genOptions.put(entry.getKey(), entry.getValue());
			}

			// Generate tables
			long start = System.currentTimeMillis();
			LexerTables l = LexicalBuilder.compile(s.getLexems(), status);
			ParserTables r = Builder.compile(s, status);
			if (l == null || r == null) {
				return false;
			}
			long generationTime = System.currentTimeMillis() - start;

			// Generate text
			start = System.currentTimeMillis();
			EvaluationContext context = createEvaluationContext(types, s, genOptions, l, r);
			TemplatesFacade env = new TemplatesFacadeExt(new GrammarIxFactory(getTemplatePackage(s), context), registry);
			env.executeTemplate(getTemplatePackage(s) + ".main", context, null, null);
			long textTime = System.currentTimeMillis() - start;
			status.report(ProcessingStatus.KIND_INFO, "lalr: " + generationTime / 1000. + "s, text: " + textTime
					/ 1000. + "s");
			return true;
		} catch (Exception t) {
			String message = "lapg: internal error: " + t.getClass().getName();
			status.report(message, options.getDebug() >= 2 ? t : null);
			return false;
		}
	}

	private String getTemplatePackage(Grammar g) {
		String result = options.getTemplateName();
		if (result != null) {
			return result;
		}

		result = (String) g.getOptions().get("lang");
		if (result != null) {
			return result;
		}

		return "java";
	}

	private boolean checkOptions(Grammar s, TemplatesRegistry registry) {
		String templPackage = getTemplatePackage(s);
		IClass cl = registry.getTypesRegistry().getClass(templPackage + ".Options", null);

		// TODO

		return true;
	}

	private ResourceRegistry createResourceRegistry() {
		List<IResourceLoader> loaders = new ArrayList<IResourceLoader>();
		for (String path : options.getIncludeFolders()) {
			IResourceLoader resourceLoader = strategy.createResourceLoader(path);
			if (resourceLoader != null) {
				loaders.add(resourceLoader);
			}
		}
		if (options.isUseDefaultTemplates()) {
			loaders.add(new ClassResourceLoader(getClass().getClassLoader(), "org/textway/lapg/gen/templates", "utf8"));
		}
		return new ResourceRegistry(loaders.toArray(new IResourceLoader[loaders.size()]));
	}

	private TemplatesRegistry createTemplateRegistry(SourceElement grammarTemplates, ResourceRegistry resources, TypesRegistry types, TemplatesStatus templatesStatus) {
		List<IBundleLoader> loaders = new ArrayList<IBundleLoader>();
		if(grammarTemplates != null) {
			loaders.add(new StringTemplateLoader(new Resource(URI.create(grammarTemplates.getResourceName()), grammarTemplates.getText(), grammarTemplates.getLine(), grammarTemplates.getOffset())));
		}
		loaders.add(new DefaultTemplateLoader(resources));
		return new TemplatesRegistry(templatesStatus, types, loaders.toArray(new IBundleLoader[loaders.size()]));
	}

	private EvaluationContext createEvaluationContext(TypesRegistry types, Grammar s, Map<String, Object> genOptions, LexerTables l, ParserTables r) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("syntax", s);
		map.put("lex", l); // new JavaIxObjectWithType(l, types.getClass("common.Lexer", null))
		map.put("parser", r);

		String templPackage = getTemplatePackage(s);
		IClass optsClass = types.getClass(templPackage + ".Options", null);
		if(optsClass != null) {
			map.put("opts", new TiInstance(optsClass, genOptions));
		} else {
			map.put("opts", genOptions);
		}


		TiInstance context = new TiInstance(types.getClass("common.Context", null), map);
		EvaluationContext evaluationContext = new EvaluationContext(context);
		evaluationContext.setVariable("util", new TemplateStaticMethods());
		evaluationContext.setVariable("context", context);
		evaluationContext.setVariable("$", "lapg_gg.sym");
		return evaluationContext;
	}

	private final class TemplatesFacadeExt extends TemplatesFacade {

		private TemplatesFacadeExt(IxFactory factory, TemplatesRegistry registry) {
			super(factory, registry);
		}

		@Override
		public void createStream(String name, String contents) {
			strategy.createFile(name, contents, status);
		}
	}

	private static final class TemplatesStatusAdapter implements TemplatesStatus {

		private final ProcessingStatus status;

		private TemplatesStatusAdapter(ProcessingStatus status) {
			this.status = status;
		}

		public void report(int kind, String message, org.textway.templates.api.SourceElement... anchors) {
			if(anchors != null) {
				SourceElement[] n = new SourceElement[anchors.length];
				for(int i = 0; i < n.length; i++) {
					n[i] = anchors[i] != null ? new TemplateSourceElementAdapter(anchors[i]) : null;
				}
				status.report(kind, message, n);
			} else {
				status.report(kind, message);
			}
		}
	}

	private static final class TemplateSourceElementAdapter implements SourceElement {

		/**
		 * template node
		 */
		private final org.textway.templates.api.SourceElement myWrapped;

		public TemplateSourceElementAdapter(org.textway.templates.api.SourceElement element) {
			this.myWrapped = element;
		}

		public int getOffset() {
			return myWrapped.getOffset();
		}

		public int getEndOffset() {
			return myWrapped.getEndOffset();
		}

		public int getLine() {
			return myWrapped.getLine();
		}

		public String getText() {
			return myWrapped.toString();
		}

		public String getResourceName() {
			return myWrapped.getResourceName();
		}
	}
}
