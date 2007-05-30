package net.sf.lapg.templates.test.cases;

import junit.framework.Assert;
import net.sf.lapg.templates.api.ClassLoaderTemplateEnvironment;

public class TestEnvironment extends ClassLoaderTemplateEnvironment {

	public TestEnvironment(ClassLoader loader, String rootPackage) {
		super(loader, rootPackage);
	}

	public void fireError(String error) {
		Assert.fail(error);
	}
}
