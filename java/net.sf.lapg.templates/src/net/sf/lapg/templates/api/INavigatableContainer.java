package net.sf.lapg.templates.api;

public interface INavigatableContainer {

	Object getByQuery(String queryString) throws EvaluationException;
}
