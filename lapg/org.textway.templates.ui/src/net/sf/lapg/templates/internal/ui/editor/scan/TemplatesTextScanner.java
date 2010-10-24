package net.sf.lapg.templates.internal.ui.editor.scan;

import net.sf.lapg.templates.internal.ui.editor.ColorManager;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;


public class TemplatesTextScanner extends RuleBasedScanner {
	public TemplatesTextScanner(ColorManager manager) {
		IRule[] rules = new IRule[1];

		// Add generic whitespace rule.
		rules[0] = new WhitespaceRule(new WhitespaceDetector());

		setRules(rules);
        setDefaultReturnToken(new Token(new TextAttribute(manager.getTextColor())));
	}
}
