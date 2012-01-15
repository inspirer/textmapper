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
package org.textway.lapg.gen;

import org.textway.lapg.api.ProcessingStatus;
import org.textway.lapg.common.AbstractProcessingStatus;
import org.textway.lapg.common.FileUtil;
import org.textway.lapg.parser.LapgGrammar;
import org.textway.lapg.parser.LapgResolver;
import org.textway.lapg.parser.LapgTree;
import org.textway.lapg.parser.LapgTree.LapgProblem;
import org.textway.lapg.parser.LapgTree.TextSource;
import org.textway.lapg.parser.TextSourceElement;
import org.textway.lapg.parser.ast.AstExpression;
import org.textway.lapg.parser.ast.AstRoot;
import org.textway.templates.types.TypesRegistry;

import java.io.InputStream;

public class SyntaxUtil {

	public static LapgGrammar parseSyntax(LapgTree.TextSource input, ProcessingStatus status, TypesRegistry types) {
		LapgTree<AstRoot> tree = LapgTree.parseInput(input);
		LapgGrammar result = null;
		if (!tree.hasErrors()) {
			result = new LapgResolver(tree, types).resolve();
		}
		if (tree.hasErrors()) {
			result = null;
			for (LapgProblem s : tree.getErrors()) {
				status.report(lapgKindToProcessingKind(s.getKind()), s.getMessage(), new SourceElementAdapter(input, s));
			}
		}
		return result;
	}

	public static AstExpression parseExpression(String input, TypesRegistry registry) {
		LapgTree<AstExpression> tree = LapgTree.parseExpression(new TextSource("", input.toCharArray(), 1));
		if (!tree.hasErrors()) {
			return tree.getRoot();
		}
		return null;
	}

	private static int lapgKindToProcessingKind(int kind) {
		switch (kind) {
			case LapgTree.KIND_FATAL:
				return ProcessingStatus.KIND_FATAL;
			case LapgTree.KIND_WARN:
				return ProcessingStatus.KIND_WARN;
		}
		return ProcessingStatus.KIND_ERROR;
	}

	private static class SourceElementAdapter implements TextSourceElement {
		private final LapgTree.TextSource source;
		private final LapgProblem problem;

		public SourceElementAdapter(LapgTree.TextSource source, LapgProblem problem) {
			this.source = source;
			this.problem = problem;
		}

		@Override
		public int getEndOffset() {
			return problem.getEndOffset();
		}

		@Override
		public int getLine() {
			return source.lineForOffset(problem.getOffset());
		}

		@Override
		public String getText() {
			return source.getText(problem.getOffset(), problem.getEndOffset());
		}

		@Override
		public int getOffset() {
			return problem.getOffset();
		}

		@Override
		public String getResourceName() {
			return source.getFile();
		}
	}

	@Deprecated
	public static LapgGrammar parseSyntax(String inputName, InputStream stream, ProcessingStatus err, TypesRegistry types) {
		String contents = FileUtil.getFileContents(stream, FileUtil.DEFAULT_ENCODING);
		return parseSyntax(new LapgTree.TextSource(inputName, contents.toCharArray(), 1), err, types);
	}
}
