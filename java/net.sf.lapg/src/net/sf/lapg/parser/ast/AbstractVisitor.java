package net.sf.lapg.parser.ast;

public class AbstractVisitor {

	public boolean visit(AstCode n) {
		return true;
	}

	public boolean visit(AstDirective n) {
		return true;
	}

	public boolean visit(AstGroupsSelector n) {
		return true;
	}

	public boolean visit(AstIdentifier n) {
		return true;
	}

	public boolean visit(AstLexeme n) {
		return true;
	}

	public boolean visit(AstNonTerm n) {
		return true;
	}

	public boolean visit(AstOption n) {
		return true;
	}

	public boolean visit(AstRegexp n) {
		return true;
	}

	public boolean visit(AstRuleSymbol n) {
		return true;
	}

	public boolean visit(AstRule n) {
		return true;
	}

	public boolean visit(AstRoot n) {
		return true;
	}

	public boolean visit(AstReference astSymbol) {
		return true;
	}
}
