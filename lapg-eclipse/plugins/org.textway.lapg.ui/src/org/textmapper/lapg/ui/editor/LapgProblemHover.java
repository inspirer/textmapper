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

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.ISourceViewer;
import org.textmapper.lapg.common.ui.editor.StructuredTextProblemHover;

public class LapgProblemHover extends StructuredTextProblemHover {

	public LapgProblemHover(final ISourceViewer sourceViewer) {
		super(sourceViewer);
	}
	
	protected boolean canShow(Annotation annotation) {
		return annotation.getType().startsWith(LapgReconcilingStrategy.ANNOTATION_PREFIX);
	}
}
