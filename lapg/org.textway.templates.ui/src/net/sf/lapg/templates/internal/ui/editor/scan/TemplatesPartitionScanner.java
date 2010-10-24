package net.sf.lapg.templates.internal.ui.editor.scan;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.PatternRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;

public class TemplatesPartitionScanner extends RuleBasedScanner implements IPartitionTokenScanner {

	public final static String SENTENCE = "__sentence";

	public TemplatesPartitionScanner() {

		final IToken tag = new Token(SENTENCE);

		final List<PatternRule> rules = new ArrayList<PatternRule>();

		rules.add(new MultiLineRule(TemplateTokens.LTP_OPEN, TemplateTokens.LTP_CLOSE, tag));

		setRules(rules.toArray(new IPredicateRule[rules.size()]));
	}

	public void setPartialRange(IDocument document, int offset, int length, String contentType, int partitionOffset) {
		// copied from RuleBasedPartitionScanner,
		// the idea seems to be to shift range to the start of the partition
		if (partitionOffset > -1) {
			int delta = offset - partitionOffset;
			if (delta > 0) {
				super.setRange(document, partitionOffset, length + delta);
				//fOffset = offset;
				return;
			}
		}
		super.setRange(document, offset, length);
	}

	@Override
	public IToken nextToken() {
		// copy from superclass, added reseting fOffset to initial value prior to next rule
		// invocation to protect from badly written rules (e.g. PatternRule) thad do not unread
		// characters if matched partially
		fTokenOffset= fOffset;
		fColumn= UNDEFINED;

		if (fRules != null) {
			for (int i= 0; i < fRules.length; i++) {
				IToken token= (fRules[i].evaluate(this));
				if (!token.isUndefined()) {
					return token;
				} else {
					// reset offset for the next rule
					fOffset = fTokenOffset;
				}
			}
		}

		if (read() == EOF) {
			return Token.EOF;
		}
		return fDefaultReturnToken;
	}
}
