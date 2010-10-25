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

import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;

public class LapgSourceSetupParticipant implements IDocumentSetupParticipant {

	private final static String[] LAPG_CONTENT_TYPES = new String[] { IPartitions.LAPG_COMMENT_MULTI,
		IPartitions.LAPG_COMMENT_LINE, IPartitions.LAPG_STRING, IPartitions.LAPG_REGEXP,
		IPartitions.LAPG_TEMPLATES, IPartitions.LAPG_ACTION };

	public void setup(IDocument document) {
		IDocumentPartitioner partitioner = new FastPartitioner(new LapgPartitionScanner(), LAPG_CONTENT_TYPES);
		if (document instanceof IDocumentExtension3) {
			IDocumentExtension3 extension3 = (IDocumentExtension3) document;
			extension3.setDocumentPartitioner(IPartitions.LAPG_PARTITIONING, partitioner);
		} else {
			document.setDocumentPartitioner(partitioner);
		}
		partitioner.connect(document);
	}
}
