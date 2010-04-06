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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import net.sf.lapg.api.Grammar;
import net.sf.lapg.parser.LapgResolver;
import net.sf.lapg.parser.LapgTree;
import net.sf.lapg.parser.LapgTree.LapgProblem;
import net.sf.lapg.parser.LapgTree.TextSource;
import net.sf.lapg.parser.ast.AstRoot;

public class SyntaxUtil {

	public static Grammar parseSyntax(String sourceName, InputStream stream, INotifier err,
			Map<String, Object> options) {
		String contents = getFileContents(stream);
		LapgTree<AstRoot> tree = LapgTree.parse(new TextSource(sourceName, contents.toCharArray(), 1));
		Grammar result = null;
		if (!tree.hasErrors()) {
			result = new LapgResolver(tree, options).resolve();
		}
		if (tree.hasErrors()) {
			result = null;
			for (LapgProblem s : tree.getErrors()) {
				err.error(s.getMessage() + "\n");
			}
		}
		return result;

	}

	public static String getFileContents(InputStream stream) {
		StringBuffer contents = new StringBuffer();
		char[] buffer = new char[2048];
		int count;
		try {
			Reader in = new InputStreamReader(stream, "utf8");
			try {
				while ((count = in.read(buffer)) > 0) {
					contents.append(buffer, 0, count);
				}
			} finally {
				in.close();
			}
		} catch (IOException ioe) {
			return null;
		}
		return contents.toString();
	}
}
