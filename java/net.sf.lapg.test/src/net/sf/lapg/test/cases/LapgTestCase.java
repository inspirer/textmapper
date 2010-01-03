package net.sf.lapg.test.cases;

import java.io.InputStream;
import org.junit.Assert;

import junit.framework.TestCase;

public abstract class LapgTestCase extends TestCase {

	protected static final String TESTCONTAINER = "net/sf/lapg/test/cases/input";
	protected static final String RESULTCONTAINER = "net/sf/lapg/test/cases/expected";

	protected InputStream openStream(String name, String root) {
		InputStream is = getClass().getClassLoader().getResourceAsStream(root + "/" + name);
		Assert.assertNotNull(is);
		return is;
	}

	protected static String removeSpaces(String input) {
		char[] c = new char[input.length()];
		input.getChars(0, input.length(), c, 0);

		int to = 0;
		for (int i = 0; i < c.length; i++) {
			if (c[i] != ' ' && c[i] != '\t') {
				c[to++] = c[i];
			}
		}

		return new String(c, 0, to);
	}
}