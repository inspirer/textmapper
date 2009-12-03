package net.sf.lapg.test.cases;

import junit.framework.TestCase;
import net.sf.lapg.common.JavaPostProcessor;

import org.junit.Assert;

public class JavaPostProcessorTest extends TestCase {

	public void testSimple() throws Exception {
		String res = new JavaPostProcessor(
				"package p;\n" +
				"\n" +
				"import xxx.A;\n" +
				"\n" +
				"class B extends xxx.@C {}").process();
		Assert.assertEquals(
				"package p;\n" +
				"\n" +
				"import xxx.A;\n" +
				"import xxx.C;\n" +
				"\n" +
				"class B extends C {}", res);
	}

	public void testSeveral() throws Exception {
		String res = new JavaPostProcessor(
				"package p;\n" +
				"\n" +
				"import xxx.A;\n" +
				"\n" +
				"class B extends xxx.@C implements yyy.@QQ {}").process();
		Assert.assertEquals(
				"package p;\n" +
				"\n" +
				"import xxx.A;\n" +
				"import xxx.C;\n" +
				"import yyy.QQ;\n" +
				"\n" +
				"class B extends C implements QQ {}", res);
	}

	public void testConflict() throws Exception {
		String res = new JavaPostProcessor(
				"package p;\n" +
				"\n" +
				"import xxx.A;\n" +
				"\n" +
				"class B extends aaa.@A implements qqq.@A {}").process();
		Assert.assertEquals(
				"package p;\n" +
				"\n" +
				"import xxx.A;\n" +
				"\n" +
				"class B extends aaa.A implements qqq.A {}", res);
	}
}
