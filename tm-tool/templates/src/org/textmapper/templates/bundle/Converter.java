/**
 * Copyright 2002-2020 Evgeny Gryaznov
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
package org.textmapper.templates.bundle;

import org.textmapper.templates.ast.Node;
import org.textmapper.templates.storage.Resource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Converter {
	public static void main(String[] args) throws IOException {
		Files.walk(new File(".").toPath())
				.filter(p -> p.toString().endsWith(".ltp"))
				.forEach(Converter::convert);
	}

	private static void convert(Path p) {
		try {
			String content = new String(Files.readAllBytes(p));
			Resource res = new Resource(p.toUri(), content);

			String name = p.getFileName().toString();
			if (!name.endsWith(".ltp")) {
				throw new RuntimeException();
			}
			name = name.substring(0, name.length() - 4);
			TemplatesBundle bundle = TemplatesBundle.parse(res, name, (kind, message, anchors) -> {
				throw new RuntimeException("broken file");
			});

			StringBuilder sb = new StringBuilder();
			for (IBundleEntity entity : bundle.getEntities()) {
				((Node) entity).toJavascript(sb);
			}

			Files.write(p.resolveSibling(name + ".js"), sb.toString().getBytes());

			System.out.println(p + " " + name);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
