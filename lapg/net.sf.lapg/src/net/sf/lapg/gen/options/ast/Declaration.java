package net.sf.lapg.gen.options.ast;

import java.util.List;
import net.sf.lapg.gen.options.OptdefTree.TextSource;

public class Declaration extends AstOptNode {

	private AnnoKind kind;
	private String name;
	private List<FeatureDeclaration> featureDeclarations;

	public Declaration(AnnoKind kind, String name, List<FeatureDeclaration> featureDeclarations, TextSource input, int start, int end) {
		super(input, start, end);
		this.kind = kind;
		this.name = name;
		this.featureDeclarations = featureDeclarations;
	}

	public AnnoKind getKind() {
		return kind;
	}
	public String getName() {
		return name;
	}
	public List<FeatureDeclaration> getFeatureDeclarations() {
		return featureDeclarations;
	}
}
