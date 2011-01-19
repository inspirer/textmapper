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

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.textway.lapg.idea.lexer.LapgTokenTypes;

public class LapgParser implements PsiParser {

	@NotNull
	public ASTNode parse(IElementType root, PsiBuilder builder) {
		final PsiBuilder.Marker file = builder.mark();
		new Parser(builder).parseGrammar();
		file.done(root);
		return builder.getTreeBuilt();
	}

	private static class Parser implements LapgTokenTypes, LapgElementTypes {

		private final PsiBuilder myBuilder;

		public Parser(PsiBuilder myBuilder) {
			this.myBuilder = myBuilder;
		}

		public void parseGrammar() {
			Marker grammar = myBuilder.mark();
			//parseOptions();
			//parseLexer();


			while (!myBuilder.eof()) {
				next();
			}
			grammar.done(GRAMMAR);
		}

		/*
		private void parseOptions() {
			while (myBuilder.getTokenType() == IDENTIFIER) {
				Marker start = myBuilder.mark();
				next();
				if (myBuilder.getTokenType() == OP_EQ) {
					next();
					parseExpression();
					start.done(OPTION);
				} else {
					start.rollbackTo();
					return;
				}
			}
		}

		private void parseLexer() {
			while(true) {
				IElementType current = myBuilder.getTokenType();
				if(!(current == OP_LBRACKET || current == IDENTIFIER)) {
					return;
				}
				Marker start = myBuilder.mark();
				next();
				if(current == OP_LBRACKET) {
					while(myBuilder.getTokenType() == ICON) {
						next();
					}
					expect(OP_RBRACKET, "']'");
					start.done(GROUPS_SELECTOR);
				} else {
					boolean hasType = false;
					if(myBuilder.getTokenType() == OP_LPAREN) {
						Marker type = myBuilder.mark();
						parseType();
						type.done(TYPE);
						hasType = true;
					}
					if(myBuilder.getTokenType() == OP_CCEQ) {
						start.rollbackTo();
						return;
					}
					if(!hasType && myBuilder.getTokenType() == OP_EQ) {
						next();
						expect(REGEXP, "regular expression");
						start.done(ALIAS_REGEXP);
					} else {
						expect(OP_COLON, "':'");
						if(myBuilder.getTokenType() == REGEXP) {
							next();
							if(myBuilder.getTokenType() == ICON) {
								next();
							}
							if(myBuilder.getTokenType() == ACTION) {
								next();
							}
						}
						start.done(LEXEME);
					}
				}
			}
		}

		private void parseType() {
			assert myBuilder.getTokenType() == OP_LPAREN;
			// '<' | '>' | '[' | ']' | identifier | '*' | '.' | ',' | '?' | '@' | '&'
			next();
			while(myBuilder.getTokenType() != OP_RPAREN) {
				IElementType current = myBuilder.getTokenType();
				if(current == OP_LT || current == OP_GT || current == OP_RBRACKET || current == OP_LBRACKET || current == IDENTIFIER
						|| current == OP_STAR || current == OP_DOT || current == OP_COMMA || current == OP_QMARK || current == OP_AT || current == OP_AND) {
					next();
				} else if(current == OP_LPAREN) {
					parseType();
				} else {
					myBuilder.error("unknown token");
					next();
				}
			}
		}


		private void parseExpression() {
			Marker expr = myBuilder.mark();
			IElementType current = myBuilder.getTokenType();
			if (current == STRING || current == ICON || current == KW_TRUE || current == KW_FALSE) {
				next();
			} else if (current == OP_LBRACKET) {
				next();
				if (myBuilder.getTokenType() != OP_RBRACKET) {
					parseExpression();
					while (myBuilder.getTokenType() == OP_COMMA) {
						next();
						parseExpression();
					}
				}
				if (myBuilder.getTokenType() != OP_RBRACKET) {
					myBuilder.error("wrong expression");
				} else {
					next();
				}
			} else if (current == IDENTIFIER) {
				Marker start = myBuilder.mark();
				next();
				if (myBuilder.getTokenType() == OP_DOT || myBuilder.getTokenType() == OP_LPAREN) {
					while (myBuilder.getTokenType() == OP_DOT) {
						next();
						expect(IDENTIFIER, "identifier");
					}
					start.done(QUALIFIED_ID);
					Marker newInstance = start.precede();
					expect(OP_LPAREN, "'('");
					if (myBuilder.getTokenType() != OP_RPAREN) {
						parseMapEntry();
						while (myBuilder.getTokenType() == OP_COMMA) {
							parseMapEntry();
						}
					}
					expect(OP_RPAREN, "')'");
					newInstance.done(NEW_INSTANCE);
				} else {
					start.done(REFERENCE);
				}
			} else {
				// TODO ??
				myBuilder.error("no expression");
				expr.drop();
				return;
			}
			expr.done(EXPRESSION);
		}

		private void parseMapEntry() {
			Marker mapEntry = myBuilder.mark();
			expect(IDENTIFIER, "identifier");
			if (myBuilder.getTokenType() == OP_COLON || myBuilder.getTokenType() == OP_EQGT || myBuilder.getTokenType() == OP_EQ) {
				next();
			} else {
				myBuilder.error("unexpected");
			}
			parseExpression();
			mapEntry.done(MAP_ENTRY);
		}

		private void expect(IElementType type, String label) {
			if (myBuilder.getTokenType() == type) {
				next();
			} else {
				myBuilder.error(label + " is expected");
			}
		}
		*/

		private void next() {
			myBuilder.advanceLexer();
		}
	}
}
