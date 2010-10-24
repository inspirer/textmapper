package net.sf.lapg.templates.internal.ui.editor.scan;

import java.util.ArrayList;
import java.util.List;

import net.sf.lapg.templates.internal.ui.editor.ColorManager;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;

public class SentenceScanner extends RuleBasedScanner {

	public SentenceScanner(ColorManager manager) {
		IToken string = new Token(new TextAttribute(manager.getStringColor()));
        final Token terminals = new Token(new TextAttribute(manager.getTerminalsColor()));
        Token template = new Token(new TextAttribute(manager.getTemplateColor()));
        Token keyword = new Token(new TextAttribute(manager.getKeywordsColor()));
        Token others = new Token(new TextAttribute(manager.getOtherColor()));

        final List<IRule> rules = new ArrayList<IRule>();

		// Add rule for double quotes
		rules.add(new SingleLineRule("\"", "\"", string, '\\'));
		// Add a rule for single quotes
		rules.add(new SingleLineRule("'", "'", string, '\\'));
		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new WhitespaceDetector()));
        rules.add(new IRule() {
            public IToken evaluate(final ICharacterScanner scanner) {
                int c = scanner.read();
                if( '$' == c ) {
                	c = scanner.read();
                	if( c == '{') {
                		return terminals;
                	} else {
                		scanner.unread();
                		scanner.unread();
                		return Token.UNDEFINED;
                	}
                } else if(c == '}') {
					return terminals;
				} else {
                    scanner.unread();
                    return Token.UNDEFINED;
                }
            }
        });

        // Add rule for define
        rules.add(new KeywordRule(template, new String[] { TemplateTokens.Ltemplate }));
        // Add rule for keywords
        rules.add(new KeywordRule(keyword, TemplateTokens.allKeywords));

		setRules(rules.toArray(new IRule[rules.size()]));
		setDefaultReturnToken(others);
	}
}
