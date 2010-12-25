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
package org.textway.lapg.gen;

import org.textway.lapg.api.*;
import org.textway.lapg.lalr.Builder;
import org.textway.lapg.lalr.ParserTables;
import org.textway.lapg.lex.LexerTables;
import org.textway.lapg.lex.LexicalBuilder;
import org.textway.lapg.parser.LapgTree.TextSource;
import org.textway.templates.api.EvaluationContext;
import org.textway.templates.api.IProblemCollector;
import org.textway.templates.api.types.IClass;
import org.textway.templates.ast.Node;
import org.textway.templates.bundle.*;
import org.textway.templates.eval.TemplatesFacade;
import org.textway.templates.objects.IxFactory;
import org.textway.templates.storage.ClassResourceLoader;
import org.textway.templates.storage.IResourceLoader;
import org.textway.templates.storage.Resource;
import org.textway.templates.storage.ResourceRegistry;
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
			ProblemCollectorAdapter collector = new ProblemCollectorAdapter(status);
			ResourceRegistry resources = createResourceRegistry();
			TypesRegistry types = new TypesRegistry(resources, collector);

			Grammar s = SyntaxUtil.parseSyntax(input, status, types);
			if (s == null || s.hasErrors()) {
				return false;
			}

			TemplatesRegistry registry = createTemplateRegistry(s.getTemplates(), resources, types, collector);
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
			EvaluationContext context = createEvaluationContext(s, genOptions, l, r);
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
		IClass cl = registry.getTypesRegistry().loadClass(templPackage + ".Options", null);

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

	private TemplatesRegistry createTemplateRegistry(SourceElement grammarTemplates, ResourceRegistry resources, TypesRegistry types, IProblemCollector problemCollector) {
		List<IBundleLoader> loaders = new ArrayList<IBundleLoader>();
		if(grammarTemplates != null) {
			loaders.add(new StringTemplateLoader(new Resource(URI.create("input"), grammarTemplates.getText(), grammarTemplates.getLine(), grammarTemplates.getOffset())));
		}
		loaders.add(new DefaultTemplateLoader(resources));
		TemplatesRegistry registry = new TemplatesRegistry(problemCollector, types, loaders.toArray(new IBundleLoader[loaders.size()]));
		return registry;
	}

	private EvaluationContext createEvaluationContext(Grammar s, Map<String, Object> genOptions, LexerTables l, ParserTables r) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("syntax", s);
		map.put("lex", l);
		map.put("parser", r);
		map.put("opts", genOptions);

		EvaluationContext context = new EvaluationContext(map);
		context.setVariable("util", new TemplateStaticMethods());
		context.setVariable("context", map);
		context.setVariable("$", "lapg_gg.sym");
		return context;
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

	private static final class ProblemCollectorAdapter implements IProblemCollector {

		private final ProcessingStatus status;

		private ProblemCollectorAdapter(ProcessingStatus status) {
			this.status = status;
		}

		public void fireError(ILocatedEntity referer, String error) {
			SourceElement adapted = null;
			if (referer instanceof Node) {
				adapted = new TemplateSourceElementAdapter((Node) referer);
			}
			if (adapted != null) {
				status.report(ProcessingStatus.KIND_ERROR, error, adapted);
			} else {
				if (referer != null) {
					error = referer.getLocation() + ": " + error;
				}
				status.report(ProcessingStatus.KIND_ERROR, error);
			}
		}
	}

	private static final class TemplateSourceElementAdapter implements SourceElement {

		/**
		 * template node
		 */
		private final Node myNode;

		public TemplateSourceElementAdapter(Node myNode) {
			this.myNode = myNode;
		}

		public int getOffset() {
			return myNode.getOffset();
		}

		public int getEndOffset() {
			return myNode.getEndOffset();
		}

		public int getLine() {
			return myNode.getLine();
		}

		public String getText() {
			return myNode.toString();
		}

		public String getResourceName() {
			return myNode.getInput().getFile();
		}
	}
}
