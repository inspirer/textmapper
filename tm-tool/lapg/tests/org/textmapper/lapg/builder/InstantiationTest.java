/**
 * Copyright 2002-2018 Evgeny Gryaznov
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
package org.textmapper.lapg.builder;

import org.junit.Test;
import org.textmapper.lapg.LapgCore;
import org.textmapper.lapg.api.*;
import org.textmapper.lapg.api.TemplateParameter.Modifier;
import org.textmapper.lapg.api.TemplateParameter.Type;
import org.textmapper.lapg.api.builder.GrammarBuilder;

import static org.junit.Assert.assertEquals;

public class InstantiationTest {

	@Test
	public void testSuffixes() {
		GrammarBuilder b = GrammarFacade.createBuilder();
		TemplateEnvironment env = GrammarFacade.createBuilder().getRootEnvironment();

		assertEquals("", env.getNonterminalSuffix());

		// Booleans
		TemplateParameter b1 = b.addParameter(Type.Flag, LapgCore.name("b1"), null, Modifier
				.Global, null);
		env = env.extend(b1, true);
		assertEquals("_B1", env.getNonterminalSuffix());

		env = env.extend(b1, false);
		assertEquals("", env.getNonterminalSuffix());
		env = env.extend(b1, true);

		// Symbols
		Symbol t1 = b.addNonterminal(LapgCore.name("T1"), null);
		Symbol t2 = b.addNonterminal(LapgCore.name("T2"), null);

		TemplateParameter s1 = b.addParameter(Type.Symbol, LapgCore.name("s1"), t1, Modifier
				.Global, null);
		TemplateParameter s2 = b.addParameter(Type.Symbol, LapgCore.name("s2"), null, Modifier.Global, null);
		env = env.extend(s1, t2);
		assertEquals("_B1_S1_T2", env.getNonterminalSuffix());

		env = env.extend(s1, null);
		assertEquals("_B1", env.getNonterminalSuffix());

		env = env.extend(s1, t1);
		assertEquals("_B1", env.getNonterminalSuffix());
	}

}
