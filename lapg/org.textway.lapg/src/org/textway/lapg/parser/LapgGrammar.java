package org.textway.lapg.parser;

import org.textway.lapg.api.Grammar;
import org.textway.lapg.api.SourceElement;
import org.textway.lapg.api.Symbol;
import org.textway.lapg.parser.ast.IAstNode;

import java.util.Map;

public class LapgGrammar {

	private final Grammar grammar;
	private final TextSourceElement templates;
	private final boolean hasErrors;
	private final Map<String, Object> options;
	private final String copyrightHeader;

	private final Map<SourceElement, IAstNode> sourceMap;
	private final Map<Symbol, String> identifierMap;
	private final Map<SourceElement, Map<String, Object>> annotationsMap;
	private final Map<SourceElement, TextSourceElement> codeMap;

	public LapgGrammar(Grammar grammar, TextSourceElement templates, boolean hasErrors, Map<String, Object> options,
			String copyrightHeader, Map<SourceElement, IAstNode> sourceMap, Map<Symbol, String> identifierMap,
			Map<SourceElement, Map<String, Object>> annotationsMap, Map<SourceElement, TextSourceElement> codeMap) {
		this.grammar = grammar;
		this.templates = templates;
		this.hasErrors = hasErrors;
		this.options = options;
		this.copyrightHeader = copyrightHeader;
		this.sourceMap = sourceMap;
		this.identifierMap = identifierMap;
		this.annotationsMap = annotationsMap;
		this.codeMap = codeMap;
	}

	public Grammar getGrammar() {
		return grammar;
	}

	public TextSourceElement getTemplates() {
		return templates;
	}

	public boolean hasErrors() {
		return hasErrors;
	}

	public Map<String, Object> getOptions() {
		return options;
	}

	public String getCopyrightHeader() {
		return copyrightHeader;
	}

	public Map<SourceElement, IAstNode> getSourceMap() {
		return sourceMap;
	}

	public Object getAnnotation(SourceElement element, String name) {
		Map<String, Object> annotations = annotationsMap.get(element);
		return annotations != null ? annotations.get(name) : null;
	}

	public TextSourceElement getCode(SourceElement element) {
		return codeMap.get(element);
	}
}
