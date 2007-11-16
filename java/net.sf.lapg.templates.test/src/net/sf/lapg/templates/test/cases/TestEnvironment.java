package net.sf.lapg.templates.test.cases;

import java.util.ArrayList;

import junit.framework.Assert;
import net.sf.lapg.templates.api.ILocatedEntity;
import net.sf.lapg.templates.api.ITemplateLoader;
import net.sf.lapg.templates.api.INavigationStrategy.Factory;
import net.sf.lapg.templates.api.impl.TemplateEnvironment;

public class TestEnvironment extends TemplateEnvironment {

	public TestEnvironment(Factory strategy, ITemplateLoader... loaders) {
		super(strategy, loaders);
	}

	public ArrayList<String> nextErrors = new ArrayList<String>();

	@Override
	public void fireError(ILocatedEntity referer, String error) {
		if( nextErrors.size() > 0 ) {
			String next = nextErrors.remove(0);
			Assert.assertEquals(next, error);
		} else {
			Assert.fail(error);
		}
	}

	public void addErrors(String... errors) {
		for( String s : errors) {
			nextErrors.add(s);
		}
	}

	public void assertEmptyErrors() {
		if( nextErrors.size() > 0) {
			Assert.fail("error is not reported: " + nextErrors.get(0) );
		}
	}
}
