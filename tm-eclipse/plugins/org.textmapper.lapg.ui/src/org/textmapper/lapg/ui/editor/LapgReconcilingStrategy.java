/**
 * This file is part of Lapg.UI project.
 *
 * Copyright (c) 2010 Evgeny Gryaznov
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Gryaznov - initial API and implementation
 */
package org.textmapper.lapg.ui.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.textmapper.lapg.api.Grammar;
import org.textmapper.lapg.common.ui.editor.ISourceStructure;
import org.textmapper.lapg.common.ui.editor.IStructuredDocumentProvider;
import org.textmapper.lapg.common.ui.editor.StructuredTextEditor;
import org.textmapper.lapg.common.ui.editor.StructuredTextReconcilingStrategy;
import org.textmapper.lapg.ui.WorkspaceResourceLoader;
import org.textmapper.lapg.ui.structure.LapgSourceStructure;
import org.textmapper.templates.api.SourceElement;
import org.textmapper.templates.api.TemplatesStatus;
import org.textmapper.templates.storage.ClassResourceLoader;
import org.textmapper.templates.storage.IResourceLoader;
import org.textmapper.templates.storage.ResourceRegistry;
import org.textmapper.templates.types.TypesRegistry;
import org.textmapper.tool.Tool;
import org.textmapper.tool.compiler.TMCompiler;
import org.textmapper.tool.compiler.TMGrammar;
import org.textmapper.tool.compiler.TMResolver;
import org.textmapper.tool.gen.LapgOptions;
import org.textmapper.tool.parser.TMTree;
import org.textmapper.tool.parser.TMTree.TMProblem;
import org.textmapper.tool.parser.TMTree.TextSource;
import org.textmapper.tool.parser.ast.TmaInput;

public class LapgReconcilingStrategy extends StructuredTextReconcilingStrategy {

	public static final String ANNOTATION_PREFIX = "org.textmapper.lapg.ui.editor"; //$NON-NLS-1$
	public static final String ANNOTATION_ERROR = ANNOTATION_PREFIX + ".error"; //$NON-NLS-1$
	public static final String ANNOTATION_WARN = ANNOTATION_PREFIX + ".warning"; //$NON-NLS-1$
	public static final String ANNOTATION_INFO = ANNOTATION_PREFIX + ".info"; //$NON-NLS-1$

	public LapgReconcilingStrategy(final LapgSourceEditor editor) {
		super(editor);
	}

	private ResourceRegistry createResourceRegistry(LapgOptions options, IProject project, List<TMProblem> problems) {
		List<IResourceLoader> loaders = new ArrayList<IResourceLoader>();
		if (options != null && options.getIncludeFolders() != null) {
			for (String path : options.getIncludeFolders()) {
				IResourceLoader resourceLoader = WorkspaceResourceLoader.create(project, path);
				if (resourceLoader != null) {
					loaders.add(resourceLoader);
				} else {
					problems.add(new TMProblem(TMTree.KIND_ERROR, 0, 0, "cannot find template folder: " + path, null));
				}
			}
		}
		if (options == null || options.isUseDefaultTemplates()) {
			loaders.add(new ClassResourceLoader(Tool.class.getClassLoader(), "org/textmapper/tool/gen/templates", "utf8"));
		}
		return new ResourceRegistry(loaders.toArray(new IResourceLoader[loaders.size()]));
	}

	@Override
	protected ISourceStructure validate(boolean first, StructuredTextEditor seditor, IDocument doc, IProgressMonitor monitor) {
		LapgSourceEditor editor = (LapgSourceEditor) seditor;
		if (!checkEditor(editor)) {
			return null;
		}
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		IFile mainResource = editor.getResource();
		Set<String> sources = new HashSet<String>();

		IStructuredDocumentProvider documentProvider = (IStructuredDocumentProvider) editor.getDocumentProvider();
		String content = doc.get();

		TextSource input = new TextSource(mainResource.getName(), content.toCharArray(), 1);
		TMTree<TmaInput> ast = TMTree.parseInput(input);
		sources.add(TMTree.PARSER_SOURCE);

		List<TMProblem> problems = ast.getErrors();
		Grammar grammar = null;
		if (problems.size() == 0) {
			LapgOptions options = editor.getOptions();

			TemplatesStatus templatesStatus = new TemplatesStatus() {
				public void report(int kind, String message,
								   SourceElement... anchors) {
					// ignore, TODO fix
				}
			};
			ResourceRegistry resources = createResourceRegistry(options, mainResource.getProject(), problems);
			TypesRegistry types = new TypesRegistry(resources, templatesStatus);

			TMCompiler resolver = new TMCompiler(ast, types);
			TMGrammar lg = resolver.resolve();
			grammar = lg != null ? lg.getGrammar() : null;
			sources.add(TMResolver.RESOLVER_SOURCE);
		}
		LapgSourceStructure model = new LapgSourceStructure(grammar, ast, mainResource);
		documentProvider.setStructure(model);

		// System.out.println("reconciled, " + problems.size() + " errors");
		reportProblems(problems, editor.getAnnotationModel(), sources);
		return model;
	}

	private void reportProblems(List<TMProblem> compilationResult, IAnnotationModel model, Set<String> sources) {
		if (compilationResult == null || model == null || model.getAnnotationIterator() == null) {
			return;
		}

		List<Annotation> annotationsToRemove = new ArrayList<Annotation>();
		for (Iterator<?> iter = model.getAnnotationIterator(); iter.hasNext(); ) {
			Annotation annotation = (Annotation) iter.next();
			if (annotation instanceof LapgAnnotation) {
				if (sources.contains(((LapgAnnotation) annotation).getSource())) {
					annotationsToRemove.add(annotation);
				}
			}
		}

		Map<Annotation, Position> annotationsToAdd = new HashMap<Annotation, Position>();
		for (TMProblem problem : compilationResult) {
			if (problem != null && problem.getOffset() >= 0 && problem.getOffset() <= problem.getEndOffset()) {
				annotationsToAdd.put(createProblemAnnotation(problem), new Position(problem.getOffset(), problem
						.getEndOffset() - problem.getOffset()));
			}
		}

		if (model instanceof IAnnotationModelExtension) {
			IAnnotationModelExtension modelExtension = (IAnnotationModelExtension) model;
			modelExtension.replaceAnnotations(annotationsToRemove.toArray(new Annotation[annotationsToRemove.size()]),
					annotationsToAdd);
		} else {
			for (Annotation annotation : annotationsToRemove) {
				model.removeAnnotation(annotation);
			}
			for (Map.Entry<Annotation, Position> entry : annotationsToAdd.entrySet()) {
				model.addAnnotation(entry.getKey(), entry.getValue());
			}
		}
	}

	private LapgAnnotation createProblemAnnotation(TMProblem problem) {
		String type;
		switch (problem.getKind()) {
			//		case TMTree.KIND_INFO:
			//			type = LapgAnnotations.ANNOTATION_INFO;
			//			break;
			case TMTree.KIND_WARN:
				type = ANNOTATION_WARN;
				break;
			default:
				type = ANNOTATION_ERROR;
				break;
		}
		return new LapgAnnotation(type, problem.getMessage(), problem.getSource());
	}

	private boolean checkEditor(final StructuredTextEditor editor) {
		return editor != null && editor.getDocumentProvider() != null;
	}

	private static class LapgAnnotation extends Annotation {

		private final String fSource;

		public LapgAnnotation(String type, String text, String source) {
			super(type, true, text);
			fSource = source;
		}

		public String getSource() {
			return fSource;
		}
	}
}
