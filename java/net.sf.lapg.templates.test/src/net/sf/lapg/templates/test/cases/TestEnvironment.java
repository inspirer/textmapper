package net.sf.lapg.templates.test.cases;

import java.util.ArrayList;

import junit.framework.Assert;
import net.sf.lapg.templates.api.ClassLoaderTemplateEnvironment;
import net.sf.lapg.templates.api.ILocatedEntity;

public class TestEnvironment extends ClassLoaderTemplateEnvironment {

	public ArrayList<String> nextErrors = new ArrayList<String>();

	public TestEnvironment(ClassLoader loader, String rootPackage) {
		super(loader, rootPackage);
	}

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
