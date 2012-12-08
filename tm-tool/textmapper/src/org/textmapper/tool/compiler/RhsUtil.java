/**
 * Copyright 2002-2012 Evgeny Gryaznov
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
package org.textmapper.tool.compiler;

import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.rule.RhsPart;
import org.textmapper.lapg.api.rule.RhsSequence;
import org.textmapper.lapg.api.rule.RhsSymbol;

/**
 * evgeny, 12/7/12
 */
public class RhsUtil {

	public static Symbol getRepresentative(RhsPart part) {
		if (part instanceof RhsSymbol) {
			return ((RhsSymbol) part).getTarget();
		}
		if (part instanceof RhsSequence) {
			RhsSequence seq = (RhsSequence) part;
			return seq.getParts().length == 1 ? getRepresentative(seq.getParts()[0]) : null;
		}
		return null;
	}
}
