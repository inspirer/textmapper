/**
 * Copyright 2010-2017 Evgeny Gryaznov
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
package org.textmapper.idea.lang.syntax.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import com.intellij.lang.PsiParser;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import org.textmapper.idea.lang.syntax.lexer.TMElementType;
import org.textmapper.idea.lang.syntax.lexer.TMTemplatesElementType;
import org.textmapper.tool.parser.TMLexer;
import org.textmapper.tool.parser.TMLexer.ErrorReporter;
import org.textmapper.tool.parser.TMLexer.Span;
import org.textmapper.tool.parser.TMLexer.Tokens;
import org.textmapper.tool.parser.TMParser;
import org.textmapper.tool.parser.TMParser.ParseException;
import org.textmapper.tool.parser.TMParser.Nonterminals;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class TMPsiParser implements PsiParser {

	private static final Map<Integer, IElementType> types = initTypes();

	private static Map<Integer, IElementType> initTypes() {
		Map<Integer, IElementType> result = new HashMap<>();
		for (IElementType t : TextmapperElementTypes.allElements) {
			int symbol = ((TMElementType) t).getSymbol();
			if (symbol >= 0) {
				result.put(symbol, t);
			}
		}
		result.put(Nonterminals.syntax_problem, TokenType.ERROR_ELEMENT);
		return result;
	}

	private static IElementType reduceType(int token) {
		return types.get(token);
	}

	@NotNull
	public ASTNode parse(IElementType root, PsiBuilder builder) {
		final PsiBuilder.Marker file = builder.mark();
		parseGrammar(builder);
		file.done(root);
		return builder.getTreeBuilt();
	}

	private void parseGrammar(PsiBuilder builder) {
		Marker grammar = builder.mark();

		TMParserEx parser = new TMParserEx(builder);
		try {
			parser.parseInput(new TMLexerEx(builder));
		} catch (IOException e) {
			/* cannot happen */
		} catch (ParseException e) {
			/* syntax error, ok */
		}

		boolean cannotRecover = !parser.markers.isEmpty();
		while (!parser.markers.isEmpty()) {
			parser.markers.pop().drop();
		}
		if (cannotRecover) {
			parser.mark().error("	syntax error");
		}
		while (!builder.eof()) {
			builder.advanceLexer();
		}
		grammar.done(TextmapperElementTypes.GRAMMAR);
	}

	private static class TMParserEx extends TMParser {

		private final PsiBuilder myBuilder;
		private final Stack<Marker> markers = new Stack<>();

		public TMParserEx(PsiBuilder builder) {
			super((message, line, offset, endoffset) -> {
				// ignore, errors are reported as syntax_problem productions
			});
			myBuilder = builder;
		}

		private Marker mark() {
			Marker m = myBuilder.mark();
			markers.push(m);
			return m;
		}

		private Marker clone(Marker inner) {
			Marker outer = inner.precede();
			free(inner, false);
			markers.push(outer);
			return outer;
		}

		private void free(Marker m, boolean drop) {
			Marker top = markers.pop();
			assert m == top;
			if (drop) {
				m.drop();
			}
		}

		private void drop(Span sym) {
			if (sym.value != null) {
				Marker m = (Marker) sym.value;
				free(m, true);
				sym.value = null;
			}
		}

		@Override
		protected void shift() throws IOException {
			Marker marker = tmNext.symbol != Tokens.eoi ? mark() : null;
			super.shift();
			tmStack[tmHead].value = marker;
		}

		@Override
		protected void applyRule(Span left, int ruleIndex, int ruleLength) {
			Span leftmost = null;
			for (int i = 0; i < ruleLength; i++) {
				if (tmStack[tmHead - i].value == null) continue;
				if (leftmost != null) {
					drop(leftmost);
				}
				leftmost = tmStack[tmHead - i];
			}
			PsiBuilder.Marker m = null;
			if (leftmost != null) {
				m = (PsiBuilder.Marker) leftmost.value;
				leftmost.value = null;
			}
			left.value = m;

			if (m != null) {
				IElementType elementType = reduceType(left.symbol);
				if (elementType != null) {
					left.value = clone(m);

					if (left.symbol == Nonterminals.syntax_problem) {
						m.error("syntax error");
					} else {
						m.done(elementType);
					}
				}
			}
			if (left.symbol == Nonterminals.input) {
				drop(left);
			}
		}

		@Override
		protected boolean restore() {
			boolean restored = super.restore();
			if (restored) {
				/* restored after syntax error - mark the location */
				tmStack[tmHead].value = mark();
			}
			return restored;
		}

		@Override
		protected void dispose(Span sym) {
			drop(sym);
		}

		@Override
		protected void cleanup(Span sym) {
			assert sym.value == null;
		}
	}

	private static class TMLexerEx extends TMLexer {
		private final PsiBuilder myBuilder;
		private Span next;

		public TMLexerEx(PsiBuilder builder) throws IOException {
			super(null, null);
			myBuilder = builder;
		}

		@Override
		public Span next() throws IOException {
			return nextInternal();
		}

		@Override
		public void reset(CharSequence input) throws IOException {
		}

		private Span nextInternal() {
			if (next != null && !myBuilder.eof()) {
				myBuilder.advanceLexer();
				while (!myBuilder.eof() && myBuilder.getTokenType() == TokenType.BAD_CHARACTER) {
					myBuilder.advanceLexer();
				}
			}
			next = new Span();
			if (myBuilder.eof()) {
				next.symbol = Tokens.eoi;
			} else if (myBuilder.getTokenType() instanceof TMTemplatesElementType) {
				TMTemplatesElementType tokenType = (TMTemplatesElementType) myBuilder.getTokenType();
				next.symbol = tokenType.getSymbol();
			} else {
				TMElementType tokenType = (TMElementType) myBuilder.getTokenType();
				next.symbol = tokenType.getSymbol();
				if (next.symbol == Nonterminals.command) {
					// temp hack
					return nextInternal();
				}
			}
			return next;
		}
	}
}
