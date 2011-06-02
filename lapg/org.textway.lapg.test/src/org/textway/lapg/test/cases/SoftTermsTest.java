/**
 * Copyright 2002-2011 Evgeny Gryaznov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.textway.lapg.test.cases;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.textway.lapg.test.cases.bootstrap.b.SampleBTree;
import org.textway.lapg.test.cases.bootstrap.b.SampleBTree.TextSource;
import org.textway.lapg.test.cases.bootstrap.b.ast.*;

/**
 * Gryaznov Evgeny, 6/3/11
 */
public class SoftTermsTest extends TestCase {

	public void testSampleB() {
		checkParsed(
				"class P {\n" +
				" class Q { }\n" +
				" extends ()\n" +
				" class E extends D { }\n" +
				" xyzzz ()\n" +
				" class E25 extends D25 { }\n" +
				" q ()\n" +
				" \n" +
				"}",

			"class:'P',class:'Q',meth:extends,class:(extends D)'E',meth:xyzzz,class:(extends D25)'E25',meth:q,");
	}

	private void checkParsed(String text, String expected) {
		SampleBTree<IAstClassdefNoEoi> tree = SampleBTree.parse(new TextSource("input", text.toCharArray(), 1));
		Assert.assertFalse(tree.hasErrors());
		Assert.assertNotNull(tree.getRoot());
		final StringBuilder sb = new StringBuilder();
		tree.getRoot().accept(new AstVisitor() {
			@Override
			protected boolean visit(AstID n) {
				sb.append("'").append(n.getIdentifier()).append("'").append(',');
				return true;
			}

			@Override
			protected boolean visit(AstClassdef n) {
				sb.append("class:");
				if(n.getIdentifier() != null) {
					sb.append("(extends ").append(n.getIdentifier()).append(")");
				}
				return true;
			}

			@Override
			protected boolean visit(AstClassdeflistItem n) {
				if(n.getClassdef() == null) {
					sb.append("meth:").append(n.getIdentifier()).append(',');
				}
				return true;
			}
		});
		Assert.assertEquals(expected, sb.toString());
	}
}
