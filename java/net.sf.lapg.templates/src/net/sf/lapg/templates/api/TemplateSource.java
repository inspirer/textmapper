package net.sf.lapg.templates.api;

import net.sf.lapg.templates.ast.AstParser;

public class TemplateSource {

	private final String sourceName;
	private final ITemplate[] templates;

	public TemplateSource(String sourceName, ITemplate[] templates) {
		this.sourceName = sourceName;
		this.templates = templates;
	}

	public String getName() {
		return sourceName;
	}

	public ITemplate[] getTemplates() {
		return templates;
	}

	public static TemplateSource buildSource(final String sourceName, String contents, String templatePackage,
			final ITemplatesFacade facade) {
		AstParser p = new AstParser() {
			@Override
			public void error(String s) {
				facade.fireError(null, sourceName + ":" + s);
			}
		};
		ITemplate[] templates;
		if (!p.parse(contents, templatePackage, sourceName)) {
			templates = new ITemplate[0];
		} else {
			templates = p.getResult();
		}
		return new TemplateSource(sourceName, templates);
	}
}
