/**
 * Copyright 2010-2017 Evgeny Gryaznov
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package org.textmapper.idea.lang.syntax.findUsages;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.SearchRequestCollector;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.search.searches.ReferencesSearch.SearchParameters;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.textmapper.idea.lang.syntax.psi.TmNamedElement;

/**
 * evgeny, 8/13/12
 */
public class TMOptReferencesSearcher extends QueryExecutorBase<PsiReference, SearchParameters> {

	public TMOptReferencesSearcher() {
		super(true);
	}

	@Override
	public void processQuery(@NotNull ReferencesSearch.SearchParameters queryParameters, @NotNull Processor<? super PsiReference> consumer) {
		PsiElement refElement = queryParameters.getElementToSearch();
		if (!(refElement instanceof TmNamedElement)) return;

		addOptionalUsages((TmNamedElement) refElement, queryParameters.getEffectiveSearchScope(), queryParameters.getOptimizer());
	}

	static void addOptionalUsages(TmNamedElement element, SearchScope scope, SearchRequestCollector collector) {
		String name = element.getName();
		if (name == null || name.startsWith("'")) return;
		final String optName = name + "opt";
		collector.searchWord(optName, scope, UsageSearchContext.IN_CODE, true, element);
	}
}
