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
package net.sf.lapg.idea.lang.parser;

import com.intellij.lang.ASTFactory;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import net.sf.lapg.idea.file.LapgFileType;

public interface LapgElementTypes {
	final IFileElementType FILE = new IFileElementType(LapgFileType.LAPG_LANGUAGE) {
		@Override
		public ASTNode parseContents(ASTNode chameleon) {
			return ASTFactory.leaf(TEXT, chameleon.getChars());
		}
	};

	public static final IElementType TEXT = new IElementType("LAPG_TEXT", LapgFileType.LAPG_LANGUAGE);
}
