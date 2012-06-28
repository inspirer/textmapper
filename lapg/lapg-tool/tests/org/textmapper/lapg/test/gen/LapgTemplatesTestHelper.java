/**
 * Copyright 2002-2012 Evgeny Gryaznov
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
package org.textmapper.lapg.test.gen;

import org.textmapper.lapg.gen.LapgGenerator;
import org.textmapper.lapg.gen.TemplateStaticMethods;
import org.textmapper.lapg.test.CheckingFileBasedStrategy;
import org.textmapper.templates.api.EvaluationContext;
import org.textmapper.templates.api.SourceElement;
import org.textmapper.templates.api.TemplatesStatus;
import org.textmapper.templates.api.types.IClass;
import org.textmapper.templates.bundle.DefaultTemplateLoader;
import org.textmapper.templates.bundle.TemplatesRegistry;
import org.textmapper.templates.eval.TemplatesFacade;
import org.textmapper.templates.objects.IxFactory;
import org.textmapper.templates.objects.JavaIxFactory;
import org.textmapper.templates.storage.ClassResourceLoader;
import org.textmapper.templates.storage.IResourceLoader;
import org.textmapper.templates.storage.ResourceRegistry;
import org.textmapper.templates.types.TiInstance;
import org.textmapper.templates.types.TypesRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Gryaznov Evgeny, 2/26/12
 */
public class LapgTemplatesTestHelper {

	public static void generationTest(String template, String folder, String[] createdFiles) {
		new LapgTemplatesTestHelper().gentest(template, folder + "/templates", folder, createdFiles);
	}

	public void gentest(String template, String templatesFolder, String folder, String[] createdFiles) {
		try {
			File root = new File(folder);
			assertTrue("folder doesn't exist: " + root.getAbsolutePath(), root.exists() && root.isDirectory());

			File tpl = new File(templatesFolder);
			assertTrue("folder doesn't exist: " + tpl.getAbsolutePath(), tpl.exists() && tpl.isDirectory());

			CheckingFileBasedStrategy strategy = new CheckingFileBasedStrategy(root);
			TemplatesStatus templatesStatus = new TemplatesStatus() {
				@Override
				public void report(int kind, String message, SourceElement... anchors) {
					String location = "";
					for (SourceElement el : anchors) {
						location += el.getResourceName() + "," + el.getLine() + ": ";
					}
					fail((kind == KIND_INFO ? "info" : kind == KIND_WARN ? "warn" : "error") + " reported: " + location + message);
				}
			};
			ResourceRegistry resources = createResourceRegistry(strategy, tpl.getAbsolutePath());
			TypesRegistry types = new TypesRegistry(resources, templatesStatus);

			Map<String, Object> genOptions = createOptions();
			EvaluationContext context = createEvaluationContext(types, genOptions);
			TemplatesFacade env = new TemplatesFacadeExt(
					new JavaIxFactory(),
					new TemplatesRegistry(templatesStatus, types, new DefaultTemplateLoader(resources)),
					strategy);
			String res = env.executeTemplate(template, context, null, null);
			assertEquals("", res);

			for (String s : createdFiles) {
				assertTrue("file is not generated: " + s, strategy.getCreated().contains(s));
			}

		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			fail(ex.getMessage());
		}
	}

	protected Map<String, Object> createOptions() {
		return new HashMap<String, Object>();
	}

	protected ResourceRegistry createResourceRegistry(CheckingFileBasedStrategy strategy, String... folders) {
		List<IResourceLoader> loaders = new ArrayList<IResourceLoader>();
		for (String path : folders) {
			IResourceLoader resourceLoader = strategy.createResourceLoader(path);
			if (resourceLoader != null) {
				loaders.add(resourceLoader);
			}
		}
		loaders.add(new ClassResourceLoader(LapgGenerator.class.getClassLoader(), "org/textway/lapg/gen/templates", "utf8"));
		return new ResourceRegistry(loaders.toArray(new IResourceLoader[loaders.size()]));
	}

	protected EvaluationContext createEvaluationContext(TypesRegistry types, Map<String, Object> genOptions) {
		Map<String, Object> map = new HashMap<String, Object>();
		String templPackage = "java";
		IClass optsClass = types.getClass(templPackage + ".Options", null);
		if (optsClass != null) {
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

	protected final class TemplatesFacadeExt extends TemplatesFacade {

		private final CheckingFileBasedStrategy strategy;

		private TemplatesFacadeExt(IxFactory factory, TemplatesRegistry registry, CheckingFileBasedStrategy strategy) {
			super(factory, registry);
			this.strategy = strategy;
		}

		@Override
		public void createStream(String name, String contents) {
			strategy.createFile(name, contents, null);
		}
	}
}
