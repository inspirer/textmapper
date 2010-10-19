package net.sf.lapg.templates.internal.ui.editor.scan;

import java.util.HashSet;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class KeywordRule implements IRule {

    private final IToken token;
    private final String[] keywords;
    private final HashSet<String> keywordsHash;

    public KeywordRule(final IToken token, final String[] keywords) {
        this.token = token;
        this.keywords = keywords;
        this.keywordsHash = new HashSet<String>(keywords.length);
        for(String s : keywords) {
        	keywordsHash.add(s);
        }
    }

    private boolean keywordExists(final String prefix) {
        for (final String w : keywords) {
            if (w.startsWith(prefix)) {
				return true;
			}
        }
        return false;
    }

    public IToken evaluate(final ICharacterScanner scanner) {
        final StringBuilder buff = new StringBuilder();
        boolean stopReading = false;
        int reads = 0;
        while (!stopReading) {
            reads++;
            final char c = (char) scanner.read();
            if ((buff.length() > 0) && !Character.isJavaIdentifierPart(c)) {
                if (keywordsHash.contains(buff.toString()) && !keywordExists(buff.toString() + c)) {
                    scanner.unread();
                    return token;
                }
            }
            buff.append(c);
            stopReading = !keywordExists(buff.toString());
        }

        for (int i = 0; i < reads; i++) {
            scanner.unread();
        }
        return Token.UNDEFINED;
    }

}
