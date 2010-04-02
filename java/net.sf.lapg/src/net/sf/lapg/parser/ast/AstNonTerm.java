package net.sf.lapg.parser.ast;

import java.util.List;
import java.util.Map;

import net.sf.lapg.parser.LapgTree.TextSource;

public class AstNonTerm extends AstNode implements AstGrammarPart {

	private final AstIdentifier name;
	private final String type;
	private final List<AstRule> rules;
	private final Map<String, Object> annotations;

	public AstNonTerm(AstIdentifier name, String type, List<AstRule> rules,
			Map<String, Object> annotations, TextSource source, int offset,
			int endoffset) {
		super(source, offset, endoffset);
		this.name = name;
		this.type = type;
		this.rules = rules;
		this.annotations = annotations;
	}

	public AstIdentifier getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public List<AstRule> getRules() {
		return rules;
	}

	public Map<String, Object> getAnnotations() {
		return annotations;
	}

	public void accept(AbstractVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if(name != null) {
			name.accept(v);
		}
		if(rules != null) {
			for(AstRule r : rules) {
				r.accept(v);
			}
		}
	}
}
