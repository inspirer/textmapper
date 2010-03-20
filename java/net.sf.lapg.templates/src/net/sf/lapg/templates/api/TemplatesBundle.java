package net.sf.lapg.templates.api;

import net.sf.lapg.templates.ast.AstParser;
import net.sf.lapg.templates.ast.AstLexer.ErrorReporter;

public class TemplatesBundle {

	private final String sourceName;
	private final IBundleEntity[] entities;

	public TemplatesBundle(String sourceName, IBundleEntity[] entities) {
		this.sourceName = sourceName;
		this.entities = entities;
	}

	public String getName() {
		return sourceName;
	}

	public IBundleEntity[] getEntities() {
		return entities;
	}

	public static TemplatesBundle parse(final String sourceName, String contents, String templatePackage,
			final IProblemCollector collector) {
		AstParser p = new AstParser(new ErrorReporter() {
			public void error(int start, int end, int line, String s) {
				collector.fireError(null, sourceName + ":" + s);
			}
		});
		IBundleEntity[] entities;
		if (!p.parse(contents, templatePackage, sourceName)) {
			entities = new IBundleEntity[0];
		} else {
			entities = p.getResult();
		}
		return new TemplatesBundle(sourceName, entities);
	}
}
