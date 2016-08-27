/**
 * Copyright (c) 2010-2016 Evgeny Gryaznov
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package org.textmapper.idea.lang.syntax.compiler;

import com.intellij.facet.FacetManager;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.psi.MultiplePsiFilesPerDocumentFileViewProvider;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.PlatformUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.textmapper.idea.compiler.TmCompilerTask;
import org.textmapper.idea.compiler.TmCompilerUtil;
import org.textmapper.idea.compiler.TmProcessingStatus;
import org.textmapper.idea.facet.TmFacet;
import org.textmapper.idea.facet.TmFacetType;
import org.textmapper.idea.facet.TmProjectSettings;
import org.textmapper.idea.facet.TmSettings;
import org.textmapper.idea.lang.syntax.compiler.TMExternalAnnotator.TMExtInput;
import org.textmapper.idea.lang.syntax.compiler.TMExternalAnnotator.TMMessage;
import org.textmapper.idea.lang.syntax.parser.TMPsiFile;
import org.textmapper.idea.lang.syntax.psi.TmGrammar;
import org.textmapper.idea.lang.syntax.psi.TmHeader;
import org.textmapper.lapg.api.*;

import java.util.ArrayList;
import java.util.List;

/**
 * evgeny, 7/19/13
 */
public class TMExternalAnnotator extends ExternalAnnotator<TMExtInput, List<TMMessage>> {

	private static final Logger LOG = Logger.getInstance("#org.textmapper.idea.lang.syntax.compiler.TMExternalAnnotator");

	@Override
	@Nullable
	public final TMExtInput collectInformation(@NotNull com.intellij.psi.PsiFile file) {
		if (file.getContext() != null || !(file instanceof TMPsiFile)) {
			return null;
		}

		if (file.getViewProvider() instanceof MultiplePsiFilesPerDocumentFileViewProvider) {
			return null;
		}

		final Module module = ModuleUtilCore.findModuleForPsiElement(file);
		if (module == null) {
			return null;
		}
		final Project project = file.getProject();
		final TmSettings settings;
		if (PlatformUtils.isIntelliJ()) {
			TmFacet facet = FacetManager.getInstance(module).getFacetByType(TmFacetType.ID);
			if (facet == null) {
				return null;
			}
			settings = facet.getConfiguration();
		} else {
			settings = TmProjectSettings.getInstance(project);
		}

		Document document = PsiDocumentManager.getInstance(project).getDocument(file);
		if (document == null) {
			return null;
		}
		String fileContent = document.getText();
		if (StringUtil.isEmptyOrSpaces(fileContent)) {
			return null;
		}
		return new TMExtInput(project, file, fileContent, settings);
	}

	@Nullable
	@Override
	public List<TMMessage> doAnnotate(TMExtInput input) {
		final AnnotatorProcessingStatus result = new AnnotatorProcessingStatus();
		TmCompilerUtil.validateFile(
				new TmCompilerTask(
						VfsUtil.virtualToIoFile(input.getFile().getVirtualFile()),
						input.getFileContent(),
						null,
						input.getSettings().isVerbose(),
						input.getSettings().isExcludeDefaultTemplates(),
						input.getSettings().getTemplatesFolder()),
				result);
		return result.getResult();
	}

	@Override
	public void apply(@NotNull PsiFile file, List<TMMessage> annotationResult, @NotNull AnnotationHolder holder) {
		for (TMMessage m : annotationResult) {
			createAnnotation(file, m, holder);
		}
	}

	private static void createAnnotation(@NotNull PsiFile file, @NotNull TMMessage message, @NotNull AnnotationHolder holder) {
		final Document document = file.getViewProvider().getDocument();
		if (document == null || message.getOffset() < 0) {
			return;
		}
		final int startOffset = message.getOffset();
		if (startOffset == 0 && message.getLength() == 0 && file instanceof TMPsiFile) {
			TmGrammar g = ((TMPsiFile) file).getGrammar();
			if (g != null) {
				TmHeader header = g.getHeader();
				if (header != null) {
					annotateElement(message, holder, header);
					return;
				}
			}
		}

		final TextRange textRange = new TextRange(startOffset, startOffset + message.getLength());
		PsiElement element = file.findElementAt(startOffset + message.getLength() / 2);
		while (element != null && textRange.getStartOffset() < element.getTextOffset()) {
			element = element.getParent();
		}
		boolean annotateByElement = element != null && textRange.equals(element.getTextRange());
		Annotation annotation = annotateByElement ? annotateElement(message, holder, element)
				: annotateTextRange(message, holder, textRange);
	}


	@Nullable
	private static Annotation annotateElement(TMMessage message, AnnotationHolder holder, @NotNull PsiElement element) {
		switch (message.getType()) {
			case ProcessingStatus.KIND_WARN:
				return holder.createWarningAnnotation(element, message.getMessage());
			case ProcessingStatus.KIND_ERROR:
			case ProcessingStatus.KIND_FATAL:
				return holder.createErrorAnnotation(element, message.getMessage());
			case ProcessingStatus.KIND_INFO:
				return holder.createInfoAnnotation(element, message.getMessage());
		}
		return null;
	}

	@Nullable
	private static Annotation annotateTextRange(TMMessage message, AnnotationHolder holder, TextRange textRange) {
		switch (message.getType()) {
			case ProcessingStatus.KIND_WARN:
				return holder.createWarningAnnotation(textRange, message.getMessage());
			case ProcessingStatus.KIND_ERROR:
			case ProcessingStatus.KIND_FATAL:
				return holder.createErrorAnnotation(textRange, message.getMessage());
			case ProcessingStatus.KIND_INFO:
				return holder.createInfoAnnotation(textRange, message.getMessage());
		}
		return null;
	}

	public static class TMExtInput {
		private final Project project;
		private final PsiFile file;
		private final String fileContent;
		private final TmSettings config;

		public TMExtInput(Project project, PsiFile file, String fileContent, TmSettings config) {
			this.project = project;
			this.file = file;
			this.fileContent = fileContent;
			this.config = config;
		}

		public Project getProject() {
			return project;
		}

		public PsiFile getFile() {
			return file;
		}

		public String getFileContent() {
			return fileContent;
		}

		public TmSettings getSettings() {
			return config;
		}
	}

	public static class TMMessage {

		private final int type;
		private final int offset;
		private final int length;
		private final String message;

		public TMMessage(int type, int offset, int length, String message) {
			this.type = type;
			this.offset = offset;
			this.length = length;
			this.message = message;
		}

		public int getOffset() {
			return offset;
		}

		public int getType() {
			return type;
		}

		public String getMessage() {
			return message;
		}

		public int getLength() {
			return length;
		}
	}

	private static class AnnotatorProcessingStatus implements TmProcessingStatus {
		private List<TMMessage> result = new ArrayList<>();

		private boolean hasErrors = false;

		private AnnotatorProcessingStatus() {
		}

		public boolean hasErrors() {
			return hasErrors;
		}

		public void report(int kind, String message, SourceElement... anchors) {
			if (kind <= KIND_ERROR) {
				hasErrors = true;
			}
			boolean reported = false;
			for (SourceElement anchor : anchors) {
				if (anchor != null) {
					reportInternal(kind, message, anchor);
					reported = true;
				}
			}
			if (!reported) {
				reportInternal(kind, message, null);
			}
		}

		private void reportInternal(int kind, String message, SourceElement anchor) {
			while (anchor instanceof DerivedSourceElement) {
				anchor = ((DerivedSourceElement) anchor).getOrigin();
			}
			if (anchor instanceof TextSourceElement) {
				int start = ((TextSourceElement) anchor).getOffset();
				int len = ((TextSourceElement) anchor).getEndoffset() - start;
				String text = ((TextSourceElement) anchor).getText();
				// TODO: add test on unicode two-byte chars
				assert len == text.length();
				while (len > 1 && isWhitespace(text.charAt(len - 1))) {
					len--;
				}
				result.add(new TMMessage(kind, start, len, message));
			} else {
				result.add(new TMMessage(kind, 0, 0, message));
			}
		}

		public void report(String message, Throwable th) {
			hasErrors = true;
			result.add(new TMMessage(ProcessingStatus.KIND_ERROR, 0, 0, message));
			LOG.error(message, th);
		}

		public void report(ParserConflict conflict) {
			if (conflict.getKind() != ParserConflict.FIXED) {
				report(KIND_ERROR, conflict.getText(), conflict.getRules());
			}
		}

		public void debug(String info) {
			// ignore
		}

		public boolean isDebugMode() {
			return false;
		}

		public boolean isAnalysisMode() {
			return false;
		}

		private List<TMMessage> getResult() {
			return result;
		}
	}

	private static boolean isWhitespace(char c) {
		return c == '\t' || c == '\r' || c == '\n' || c == ' ';
	}
}
