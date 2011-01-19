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
package org.textway.lapg.idea.parser;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import org.textway.lapg.idea.file.LapgFileType;
import org.textway.lapg.idea.lexer.LapgElementType;

public interface LapgElementTypes {
	final IFileElementType FILE = new IFileElementType(LapgFileType.LAPG_LANGUAGE);

	public static final IElementType GRAMMAR = new LapgElementType("grammar");

//	public static final IElementType OPTION = new LapgElementType("option");
//	public static final IElementType EXPRESSION = new LapgElementType("expression");
//	public static final IElementType REFERENCE = new LapgElementType("reference");
//	public static final IElementType QUALIFIED_ID = new LapgElementType("qualified identifier");
//	public static final IElementType NEW_INSTANCE = new LapgElementType("type instantiation");
//	public static final IElementType MAP_ENTRY = new LapgElementType("map entry");
//
//	public static final IElementType GROUPS_SELECTOR = new LapgElementType("groups selector");
//	public static final IElementType TYPE = new LapgElementType("type");
//	public static final IElementType ALIAS_REGEXP = new LapgElementType("alias");
//	public static final IElementType LEXEME = new LapgElementType("lexeme");
}
