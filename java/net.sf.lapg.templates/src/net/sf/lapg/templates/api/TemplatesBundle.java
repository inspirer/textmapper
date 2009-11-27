package net.sf.lapg.templates.api;

import net.sf.lapg.templates.ast.AstParser;
import net.sf.lapg.templates.ast.AstLexer.ErrorReporter;

public class TemplatesBundle {

	private final String sourceName;
	private final ITemplate[] templates;

	public TemplatesBundle(String sourceName, ITemplate[] templates) {
		this.sourceName = sourceName;
		this.templates = templates;
	}

	public String getName() {
		return sourceName;
	}

	public ITemplate[] getTemplates() {
		return templates;
	}

	public static ITemplate[] parse(final String sourceName, String contents, String templatePackage,
			final IProblemCollector collector) {
		AstParser p = new AstParser(new ErrorReporter() {
			@Override
			public void error(int start, int end, int line, String s) {
				collector.fireError(null, sourceName + ":" + s);
			}
		});
		ITemplate[] templates;
		if (!p.parse(contents, templatePackage, sourceName)) {
			templates = new ITemplate[0];
		} else {
			templates = p.getResult();
		}
		return templates;
	}
}
