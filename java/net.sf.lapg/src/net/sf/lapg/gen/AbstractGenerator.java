/*************************************************************
 * Copyright (c) 2002-2009 Evgeny Gryaznov
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Gryaznov - initial API and implementation
 *************************************************************/
package net.sf.lapg.gen;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.lapg.INotifier;
import net.sf.lapg.LexerTables;
import net.sf.lapg.ParserTables;
import net.sf.lapg.api.Grammar;
import net.sf.lapg.input.SyntaxUtil;
import net.sf.lapg.lalr.Builder;
import net.sf.lapg.lex.LexicalBuilder;
import net.sf.lapg.templates.api.EvaluationContext;
import net.sf.lapg.templates.api.IEvaluationStrategy;
import net.sf.lapg.templates.api.ILocatedEntity;
import net.sf.lapg.templates.api.ITemplateLoader;
import net.sf.lapg.templates.api.INavigationStrategy.Factory;
import net.sf.lapg.templates.api.impl.ClassTemplateLoader;
import net.sf.lapg.templates.api.impl.DefaultEvaluationStrategy;
import net.sf.lapg.templates.api.impl.StringTemplateLoader;
import net.sf.lapg.templates.api.impl.TemplatesFacade;
import net.sf.lapg.templates.api.impl.TemplatesRegistry;

public abstract class AbstractGenerator {

	protected final LapgOptions options;

	public AbstractGenerator(LapgOptions options) {
		this.options = options;
	}

	protected abstract ITemplateLoader createTemplateLoader(String path);

	protected abstract INotifier createNotifier();

	protected abstract void createFile(String name, String contents);

	protected abstract InputStream openInput(INotifier err);

	private Map<String, String> getDefaultOptions() {
		Map<String, String> d = new HashMap<String, String>();
		d.put("class", "Parser");
		d.put("lexer", "Lexer");
		d.put("errorprefix", "");
		d.put("breaks", "on");
		d.put("lang", "java");
		d.put("positioning", "line");
		d.put("lexemend", "off");
		d.put("maxtoken", "2048");
		d.put("stack", "1024");
		d.put("packLexems", "false");
		d.put("packTables", "false");
		return d;
	}

	public boolean compileGrammar() {
		INotifier err = createNotifier();
		try {
			InputStream is = openInput(err);
			if (is == null) {
				return false;
			}

			Grammar s = SyntaxUtil.parseSyntax(options.getInput(), is, err, getDefaultOptions());
			if (s.hasErrors()) {
				return false;
			}

			Map<String, String> genOptions = new HashMap<String, String>(s.getOptions());
			Map<String, String> additional = options.getAdditionalOptions();
			for (String key : additional.keySet()) {
				genOptions.put(key, additional.get(key));
			}

			LexerTables l = LexicalBuilder.compile(s.getLexems(), err, options.getDebug());
			ParserTables r = Builder.compile(s, err, options.getDebug());

			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("syntax", s);
			map.put("lex", l);
			map.put("parser", r);
			map.put("opts", genOptions);
			generateOutput(map, s.getTemplates(), err);
			return true;
		} catch (Throwable t) {
			err.error("lapg: internal error: " + t.getClass().getName() + "\n");
			if (options.getDebug() >= 2) {
				err.trace(t);
			}
			return false;
		} finally {
			err.dispose();
		}
	}

	private void generateOutput(Map<String, Object> map, String grammarTemplates, final INotifier notifier) {

		List<ITemplateLoader> loaders = new ArrayList<ITemplateLoader>();
		loaders.add(new StringTemplateLoader("input", grammarTemplates)); // TODO create with initial location
		for (String path : options.getIncludeFolders()) {
			ITemplateLoader tl = createTemplateLoader(path);
			if (tl != null) {
				loaders.add(tl);
			}
		}
		if (options.isUseDefaultTemplates()) {
			loaders.add(new ClassTemplateLoader(getClass().getClassLoader(), "net/sf/lapg/gen/templates", "utf8"));
		}

		TemplatesFacade env = new TemplatesFacadeExt(new GrammarNavigationFactory(options.getTemplateName()), loaders.toArray(new ITemplateLoader[loaders.size()]), notifier);
		EvaluationContext context = new EvaluationContext(map);
		context.setVariable("util", new TemplateStaticMethods());
		context.setVariable("$", "lapg_gg.sym");
		env.executeTemplate(options.getTemplateName() + ".main", context, null, null);
	}

	private final class TemplatesFacadeExt extends TemplatesFacade {

		private final INotifier notifier;

		private TemplatesFacadeExt(Factory strategy, ITemplateLoader[] loaders, INotifier notifier) {
			super(strategy, loaders);
			this.notifier = notifier;
		}

		@Override
		protected IEvaluationStrategy createEvaluationStrategy(Factory factory, TemplatesRegistry registry) {
			// TODO Auto-generated method stub
			return new DefaultEvaluationStrategy(this, factory, registry) {
				@Override
				public void createFile(String name, String contents) {
					AbstractGenerator.this.createFile(name, contents);
				}
			};
		}

		@Override
		public void fireError(ILocatedEntity referer, String error) {
			if( referer != null ) {
				notifier.error(referer.getLocation() + ": ");
			}
			notifier.error(error + "\n");
		}
	}
}
