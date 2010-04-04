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
package net.sf.lapg.ui.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.IReconcilingStrategyExtension;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;

import net.sf.lapg.ui.LapgUIActivator;
import net.sf.lapg.api.Grammar;
import net.sf.lapg.gen.AbstractGenerator;
import net.sf.lapg.parser.LapgResolver;
import net.sf.lapg.parser.LapgTree;
import net.sf.lapg.parser.LapgTree.LapgProblem;
import net.sf.lapg.parser.LapgTree.TextSource;
import net.sf.lapg.parser.ast.AstRoot;

public class LapgReconcilingStrategy implements IReconcilingStrategy, IReconcilingStrategyExtension {

	public static final String ANNOTATION_PREFIX = "net.sf.lapg.ui.editor"; //$NON-NLS-1$
	public static final String ANNOTATION_ERROR = ANNOTATION_PREFIX + ".error"; //$NON-NLS-1$
	public static final String ANNOTATION_WARN = ANNOTATION_PREFIX + ".warning"; //$NON-NLS-1$
	public static final String ANNOTATION_INFO = ANNOTATION_PREFIX + ".info"; //$NON-NLS-1$

	private IProgressMonitor fMonitor;
	private IDocument fDocument;
	private final LapgSourceEditor fEditor;

	public LapgReconcilingStrategy(final LapgSourceEditor editor) {
		fEditor = editor;
	}

	public void setDocument(final IDocument document) {
		fDocument = document;
	}

	public void reconcile(final DirtyRegion dirtyRegion, final IRegion subRegion) {
		reconcileInternal(false);
	}

	public void reconcile(final IRegion partition) {
		reconcileInternal(false);
	}

	public void setProgressMonitor(final IProgressMonitor monitor) {
		fMonitor = monitor;
	}

	public void initialReconcile() {
		reconcileInternal(true);
	}

	private void reconcileInternal(boolean first) {
		try {
			fEditor.aboutToBeReconciled();
			validate(fEditor, fDocument, fMonitor);
		} catch (Throwable ex) {
			ex.printStackTrace();
			LapgUIActivator.log(ex);
		} finally {
			fEditor.reconciled(fMonitor);
		}
	}

	private void validate(final LapgSourceEditor editor, final IDocument doc, IProgressMonitor monitor) {
		if (!checkEditor(editor)) {
			return;
		}
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		IFile mainResource = fEditor.getResource();
		Set<String> sources = new HashSet<String>();
		String content = doc.get();

		TextSource input = new TextSource(mainResource.getName(), content.toCharArray(), 1);
		LapgTree<AstRoot> ast = LapgTree.parse(input);
		sources.add(LapgTree.PARSER_SOURCE);

		List<LapgProblem> problems = ast.getErrors();
		if(problems.size() == 0) {
			LapgResolver resolver = new LapgResolver(ast, AbstractGenerator.getDefaultOptions());
			Grammar g = resolver.resolve();
			sources.add(LapgResolver.RESOLVER_SOURCE);
		}

		//System.out.println("reconciled, " + problems.size() + " errors");
		reportProblems(problems, editor.getAnnotationModel(), sources);
	}

	private void reportProblems(List<LapgProblem> compilationResult, IAnnotationModel model, Set<String> sources) {
		if (compilationResult == null || model == null || model.getAnnotationIterator() == null) {
			return;
		}

		List<Annotation> annotationsToRemove = new ArrayList<Annotation>();
		for (Iterator<?> iter = model.getAnnotationIterator(); iter.hasNext();) {
			Annotation annotation = (Annotation) iter.next();
			if (annotation instanceof LapgAnnotation) {
				if (sources.contains(((LapgAnnotation) annotation).getSource())) {
					annotationsToRemove.add(annotation);
				}
			}
		}

		Map<Annotation, Position> annotationsToAdd = new HashMap<Annotation, Position>();
		for (LapgProblem problem : compilationResult) {
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

	private LapgAnnotation createProblemAnnotation(LapgProblem problem) {
		String type;
		switch (problem.getKind()) {
		//		case LapgTree.KIND_INFO:
		//			type = LapgAnnotations.ANNOTATION_INFO;
		//			break;
		case LapgTree.KIND_WARN:
			type = ANNOTATION_WARN;
			break;
		default:
			type = ANNOTATION_ERROR;
			break;
		}
		return new LapgAnnotation(type, problem.getMessage(), problem.getSource());
	}

	private boolean checkEditor(final LapgSourceEditor editor) {
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
