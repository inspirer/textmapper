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
package org.textway.lapg.test.gen;

import org.textway.lapg.gen.LapgGenerator;
import org.textway.lapg.gen.TemplateStaticMethods;
import org.textway.templates.api.EvaluationContext;
import org.textway.templates.api.SourceElement;
import org.textway.templates.api.TemplatesStatus;
import org.textway.templates.api.types.IClass;
import org.textway.templates.bundle.DefaultTemplateLoader;
import org.textway.templates.bundle.TemplatesRegistry;
import org.textway.templates.eval.TemplatesFacade;
import org.textway.templates.objects.IxFactory;
import org.textway.templates.objects.JavaIxFactory;
import org.textway.templates.storage.ClassResourceLoader;
import org.textway.templates.storage.IResourceLoader;
import org.textway.templates.storage.ResourceRegistry;
import org.textway.templates.types.TiInstance;
import org.textway.templates.types.TypesRegistry;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Gryaznov Evgeny, 2/26/12
 */
public class LapgTemplatesTestUtil {

	public static void generationTest(String template, String folder, String[] createdFiles) {
		try {
			File root = new File(folder);
			assertTrue("folder doesn't exist: " + root.getAbsolutePath(), root.exists() && root.isDirectory());

			File tpl = new File(root, "templates");
			assertTrue("folder doesn't exist: " + tpl.getAbsolutePath(), tpl.exists() && tpl.isDirectory());

			CheckingFileBasedStrategy strategy = new CheckingFileBasedStrategy(root);
			TemplatesStatus templatesStatus = new TemplatesStatus() {
				@Override
				public void report(int kind, String message, SourceElement... anchors) {
					fail((kind == KIND_INFO ? "info" : kind == KIND_WARN ? "warn" : "error") + " reported: " + message);
				}
			};
			ResourceRegistry resources = createResourceRegistry(strategy, tpl.getAbsolutePath());
			TypesRegistry types = new TypesRegistry(resources, templatesStatus);

			Map<String, Object> genOptions = new HashMap<String, Object>();
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

	private static ResourceRegistry createResourceRegistry(CheckingFileBasedStrategy strategy, String... folders) {
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

	private static EvaluationContext createEvaluationContext(TypesRegistry types, Map<String, Object> genOptions) {
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

	private static final class TemplatesFacadeExt extends TemplatesFacade {

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
