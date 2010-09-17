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
package net.sf.lapg.gen;

import net.sf.lapg.api.Grammar;
import net.sf.lapg.api.ProcessingStatus;
import net.sf.lapg.api.ProcessingStrategy;
import net.sf.lapg.api.SourceElement;
import net.sf.lapg.lalr.Builder;
import net.sf.lapg.lalr.ParserTables;
import net.sf.lapg.lex.LexerTables;
import net.sf.lapg.lex.LexicalBuilder;
import net.sf.lapg.parser.LapgTree.TextSource;
import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.IBundleLoader;
import net.sf.lapg.templates.api.ILocatedEntity;
import net.sf.lapg.templates.api.INavigationStrategy.Factory;
import net.sf.lapg.templates.api.IProblemCollector;
import net.sf.lapg.templates.api.impl.ClassTemplateLoader;
import net.sf.lapg.templates.api.impl.StringTemplateLoader;
import net.sf.lapg.templates.api.impl.TemplatesFacade;
import net.sf.lapg.templates.api.impl.TemplatesRegistry;
import net.sf.lapg.templates.ast.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LapgGenerator {

	private final LapgOptions options;
	private final ProcessingStatus status;
	private final ProcessingStrategy strategy;

	public LapgGenerator(LapgOptions options, ProcessingStatus status, ProcessingStrategy strategy) {
		this.options = options;
		this.status = status;
		this.strategy = strategy;
	}

	/** TODO read from templates */
	public static Map<String, Object> getDefaultOptions() {
		Map<String, Object> d = new HashMap<String, Object>();
		d.put("prefix", "");
		d.put("breaks", "on");
		d.put("lang", "java");
		d.put("maxtoken", "2048");
		d.put("stack", "1024");
		d.put("packLexems", "false");
		d.put("packTables", "false");
		d.put("positions", "line,offset");
		d.put("endpositions", "");
		d.put("gentree", "false");
		d.put("genast", "false");
		return d;
	}

	public boolean compileGrammar(TextSource input) {
		try {
			Grammar s = SyntaxUtil.parseSyntax(input, status, getDefaultOptions());
			if (s == null || s.hasErrors()) {
				return false;
			}

			Map<String, Object> genOptions = new HashMap<String, Object>(s.getOptions());
			Map<String, Object> additional = options.getAdditionalOptions();
			for (String key : additional.keySet()) {
				genOptions.put(key, additional.get(key));
			}

			long start = System.currentTimeMillis();
			LexerTables l = LexicalBuilder.compile(s.getLexems(), status);
			ParserTables r = Builder.compile(s, status);
			if (l == null || r == null) {
				return false;
			}
			long generationTime = System.currentTimeMillis() - start;

			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("syntax", s);
			map.put("lex", l);
			map.put("parser", r);
			map.put("opts", genOptions);

			start = System.currentTimeMillis();
			generateOutput(map, s.getTemplates());
			long textTime = System.currentTimeMillis() - start;
			status.report(ProcessingStatus.KIND_INFO, "lalr: " + generationTime / 1000. + "s, text: " + textTime
					/ 1000. + "s");
			return true;
		} catch (Throwable t) {
			String message = "lapg: internal error: " + t.getClass().getName();
			status.report(message, options.getDebug() >= 2 ? t : null);
			return false;
		}
	}

	private void generateOutput(Map<String, Object> map, String grammarTemplates) {

		List<IBundleLoader> loaders = new ArrayList<IBundleLoader>();
		loaders.add(new StringTemplateLoader("input", grammarTemplates)); // TODO create with initial location
		for (String path : options.getIncludeFolders()) {
			IBundleLoader tl = strategy.createTemplateLoader(path);
			if (tl != null) {
				loaders.add(tl);
			}
		}
		if (options.isUseDefaultTemplates()) {
			loaders.add(new ClassTemplateLoader(getClass().getClassLoader(), "net/sf/lapg/gen/templates", "utf8"));
		}
		IProblemCollector problemCollector = new ProblemCollectorAdapter(status);
		TemplatesRegistry registry = new TemplatesRegistry(problemCollector, loaders.toArray(new IBundleLoader[loaders.size()]));

		EvaluationContext context = new EvaluationContext(map);
		context.setVariable("util", new TemplateStaticMethods());
		context.setVariable("context", map);
		context.setVariable("$", "lapg_gg.sym");
		TemplatesFacade env = new TemplatesFacadeExt(new GrammarNavigationFactory(options.getTemplateName(), context), registry, problemCollector);
		env.executeTemplate(options.getTemplateName() + ".main", context, null, null);
	}

	private final class TemplatesFacadeExt extends TemplatesFacade {

		private TemplatesFacadeExt(Factory factory, TemplatesRegistry registry, IProblemCollector collector) {
			super(factory, registry, collector);
		}

		@Override
		public void createFile(String name, String contents) {
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

		/** template node */
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

		public String getResourceName() {
			return myNode.getInput().getFile();
		}
	}
}
