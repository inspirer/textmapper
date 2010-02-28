package net.sf.lapg.templates.api;

import net.sf.lapg.templates.ast.AstParser;
import net.sf.lapg.templates.ast.AstLexer.ErrorReporter;

public class TemplatesBundle {

	private final String sourceName;
	private final ITemplate[] templates;
	private final IQuery[] queries;

	public TemplatesBundle(String sourceName, ITemplate[] templates, IQuery[] queries) {
		this.sourceName = sourceName;
		this.templates = templates;
		this.queries = queries;
	}

	public String getName() {
		return sourceName;
	}

	public ITemplate[] getTemplates() {
		return templates;
	}

	public IQuery[] getQueries() {
		return queries;
	}

	public static TemplatesBundle parse(final String sourceName, String contents, String templatePackage,
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
		return new TemplatesBundle(sourceName, templates, null);
	}
}
