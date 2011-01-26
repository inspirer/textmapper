/**
 * Copyright 2002-2011 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import org.textway.lapg.parser.LapgParser.Tokens;

public interface LapgElementTypes {
	final IFileElementType FILE = new IFileElementType(LapgFileType.LAPG_LANGUAGE);

	public static final IElementType GRAMMAR = new LapgElementType(Tokens.input, "grammar");

	public static final IElementType ACTION = new LapgElementType(Tokens.command, "action");
	public static final IElementType OPTION = new LapgElementType(Tokens.option, "option");
	public static final IElementType EXPRESSION = new LapgElementType(Tokens.expression, "expression");
	public static final IElementType REFERENCE = new LapgElementType(Tokens.reference, "reference");
	public static final IElementType SYMBOL = new LapgElementType(Tokens.symbol, "symbol");
	public static final IElementType QUALIFIED_ID = new LapgElementType(Tokens.qualified_id, "qualified identifier");
	public static final IElementType RULE = new LapgElementType(Tokens.rule0, "rule");
	public static final IElementType ANNOTATION = new LapgElementType(Tokens.annotation, "annotation");

	public static final IElementType[] allElements = {
		OPTION, EXPRESSION, REFERENCE, SYMBOL, QUALIFIED_ID, RULE, ANNOTATION, ACTION
	};

	public static final IElementType LEXEM = new LapgElementType(Tokens.lexer_part, "lexem");
	public static final IElementType NONTERM = new LapgElementType(Tokens.grammar_part, "non-terminal declaration");
}
