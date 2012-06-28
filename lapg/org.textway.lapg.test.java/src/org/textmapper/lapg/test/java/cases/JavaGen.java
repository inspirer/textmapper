package org.textmapper.lapg.test.java.cases;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.internal.compiler.CompilationResult;

import org.textmapper.lapg.test.java.JCompiler;
import junit.framework.Assert;
import junit.framework.TestCase;

public class JavaGen extends TestCase {

	public void testSimple() {
		JCompiler c = new JCompiler();
		c.addSource("a.b.c", "package a.b;\nclass c extends c2 { }");
		c.addSource("a.b.c2", "package a.b;\nclass c2 { }");
		c.compile();

		for(CompilationResult r : c.getCompilationResults()) {
			CategorizedProblem[] allProblems = r.getAllProblems();
			if(allProblems == null) {
				continue;
			}
			for(CategorizedProblem p : allProblems) {
				if(p.isError()) {
					Assert.fail("error: " + p.getMessage());
				}
			}
		}
	}
}
