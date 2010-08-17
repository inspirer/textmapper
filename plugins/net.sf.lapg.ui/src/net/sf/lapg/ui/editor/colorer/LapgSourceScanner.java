/**
 * This file is part of Lapg.UI project.
 * 
 * Copyright (c) 2010 Evgeny Gryaznov
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Gryaznov - initial API and implementation
 */
package net.sf.lapg.ui.editor.colorer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.NumberRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

import net.sf.lapg.common.ui.editor.colorer.DefaultHighlightingManager;
import net.sf.lapg.common.ui.editor.colorer.ICommonColors;
import net.sf.lapg.common.ui.editor.colorer.CommentScanner.DefaultKeywordDetector;
import net.sf.lapg.common.ui.editor.colorer.CommentScanner.WhitespaceDetector;

public class LapgSourceScanner extends BufferedRuleBasedScanner {

	public LapgSourceScanner(DefaultHighlightingManager manager) {
		List<IRule> rules = new ArrayList<IRule>();

		rules.add(new WhitespaceRule(new WhitespaceDetector()));

		rules.add(new OperatorRule(manager.getColor(ICommonColors.COLOR_OPERATORS).createToken()));

		rules.add(new BracketRule(manager.getColor(ICommonColors.COLOR_BRACKETS).createToken()));

		rules.add(new NumberRule(manager.getColor(ICommonColors.COLOR_NUMBER).createToken()));

		// Add word rule for keywords, types, and constants.
		WordRule wordRule = new WordRule(new DefaultKeywordDetector(), manager.getColor(ICommonColors.COLOR_IDENTIFIER)
				.createToken());
		IToken keyword = manager.getColor(ICommonColors.COLOR_KEYWORD).createToken();
		for (String s : fgKeywords) {
			wordRule.addWord(s, keyword);
		}
		rules.add(wordRule);

		setRules(rules.toArray(new IRule[rules.size()]));
		setDefaultReturnToken(manager.getColor(ICommonColors.COLOR_DEFAULT).createToken());
	}

	private static final class OperatorRule implements IRule {

		private final char[] OPERATORS = { ';', '.', '=', '/', '\\', '+', '-', '*', '<', '>', ':', '?', '!', ',', '|',
				'&', '^', '%', '~' };
		private final IToken fToken;

		/**
		 * Creates a new operator rule.
		 */
		public OperatorRule(IToken token) {
			fToken = token;
		}

		/**
		 * Is this character an operator character?
		 */
		public boolean isOperator(char character) {
			for (char element : OPERATORS) {
				if (element == character) {
					return true;
				}
			}
			return false;
		}

		public IToken evaluate(ICharacterScanner scanner) {

			int character = scanner.read();
			if (isOperator((char) character)) {
				do {
					character = scanner.read();
				} while (isOperator((char) character));
				scanner.unread();
				return fToken;
			} else {
				scanner.unread();
				return Token.UNDEFINED;
			}
		}
	}

	private static final class BracketRule implements IRule {

		private final char[] JAVA_BRACKETS = { '(', ')', '{', '}', '[', ']' };
		/**
		 * Token to return for this rule
		 */
		private final IToken fToken;

		/**
		 * Creates a new bracket rule.
		 */
		public BracketRule(IToken token) {
			fToken = token;
		}

		/**
		 * Is this character a bracket character?
		 */
		public boolean isBracket(char character) {
			for (char element : JAVA_BRACKETS) {
				if (element == character) {
					return true;
				}
			}
			return false;
		}

		public IToken evaluate(ICharacterScanner scanner) {

			int character = scanner.read();
			if (isBracket((char) character)) {
				do {
					character = scanner.read();
				} while (isBracket((char) character));
				scanner.unread();
				return fToken;
			} else {
				scanner.unread();
				return Token.UNDEFINED;
			}
		}
	}

	private static String[] fgKeywords = { 	};
}
