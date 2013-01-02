/**
 * Copyright 2002-2013 Evgeny Gryaznov
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
package org.textmapper.tool.test.bootstrap.nla;

import org.textmapper.lapg.common.FileUtil;
import org.textmapper.tool.test.bootstrap.nla.NlaTestTree.TextSource;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Gryaznov Evgeny, 9/30/11
 */
public class NlaTestMain {

	public static void main(String[] args) throws FileNotFoundException {
		if (args.length < 1) {
			System.err.println("no input");
			System.exit(1);
		}

		String input = args[0];
		String fileContents = FileUtil.getFileContents(new FileInputStream(input), "utf-8");
		NlaTestTree<Object> parse = NlaTestTree.parse(new TextSource(input, fileContents.toCharArray(), 1));
		if (parse.hasErrors()) {
			System.err.println("bad input");
			System.exit(1);
		}
	}
}
