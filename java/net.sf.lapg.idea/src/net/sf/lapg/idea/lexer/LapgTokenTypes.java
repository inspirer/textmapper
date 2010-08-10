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
package net.sf.lapg.idea.lexer;

import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

public interface LapgTokenTypes {
	IElementType WHITESPACE = TokenType.WHITE_SPACE;
	TokenSet whitespace = TokenSet.create(WHITESPACE);

	IElementType COMMENT = new LapgElementType("comment");
	TokenSet comments = TokenSet.create(COMMENT);

	IElementType STRING = new LapgElementType("string");
	TokenSet strings = TokenSet.create(STRING);

	IElementType TEXT = new LapgElementType("text");
	IElementType REGEXP = new LapgElementType("regexp");
	IElementType ACTION = new LapgElementType("action");
	IElementType TEMPLATES = new LapgElementType("templates");
}
