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
package org.textmapper.lapg.common.ui.editor.colorer;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.rules.BufferedRuleBasedScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

public class CommentScanner extends BufferedRuleBasedScanner {

	public static class DefaultKeywordDetector implements IWordDetector {
		public boolean isWordPart(char character) {
			return Character.isJavaIdentifierPart(character);
		}

		public boolean isWordStart(char character) {
			return Character.isJavaIdentifierStart(character);
		}
	}

	public static class WhitespaceDetector implements IWhitespaceDetector {
		public boolean isWhitespace(char character) {
			return Character.isWhitespace(character);
		}
	}

	public CommentScanner(DefaultHighlightingManager manager, String colorId) {
		IToken keyWord = manager.getColor(ICommonColors.COLOR_TASK).createToken();
		IToken defaultToken = manager.getColor(colorId).createToken();

		List<IRule> rules = new ArrayList<IRule>();
		rules.add(new WhitespaceRule(new WhitespaceDetector()));

		WordRule wordRule = new WordRule(new DefaultKeywordDetector(), defaultToken);
		wordRule.addWord("TODO", keyWord);
		wordRule.addWord("FIXME", keyWord);
		wordRule.addWord("XXX", keyWord);
		rules.add(wordRule);

		setRules(rules.toArray(new IRule[rules.size()]));
		setDefaultReturnToken(defaultToken);
	}
}
