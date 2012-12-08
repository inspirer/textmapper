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
package org.textmapper.lapg.builder;

import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.rule.RhsPart;
import org.textmapper.lapg.api.rule.RhsSequence;
import org.textmapper.lapg.api.rule.RhsSymbol;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * evgeny, 12/5/12
 */
class LiRhsSequence extends LiRhsPart implements RhsSequence {

	private final LiRhsPart[] parts;

	LiRhsSequence(LiRhsPart[] parts, SourceElement origin) {
		super(origin);
		this.parts = parts;
	}

	@Override
	public RhsPart[] getParts() {
		return parts;
	}

	@Override
	List<RhsSymbol[]> expand() {
		return expandList(parts);
	}

	static List<RhsSymbol[]> expandList(LiRhsPart[] list) {
		boolean simplePartsOnly = true;
		for (RhsPart part : list) {
			if (!(part instanceof RhsSymbol)) {
				simplePartsOnly = false;
				break;
			}
		}
		if (simplePartsOnly) {
			RhsSymbol[] parts = new RhsSymbol[list.length];
			System.arraycopy(list, 0, parts, 0, parts.length);
			return Collections.singletonList(parts);

		} else {
			List<RhsSymbol[]> result = Collections.singletonList(new RhsSymbol[0]);
			for (LiRhsPart part : list) {
				List<RhsSymbol[]> val = part.expand();
				result = cartesianProduct(result, val);
			}
			return result;
		}
	}

	private static List<RhsSymbol[]> cartesianProduct(List<RhsSymbol[]> left, List<RhsSymbol[]> right) {
		List<RhsSymbol[]> result = new ArrayList<RhsSymbol[]>(left.size() * right.size());
		for (RhsSymbol[] leftElement : left) {
			for (RhsSymbol[] rightElement : right) {
				RhsSymbol[] elem = new RhsSymbol[leftElement.length + rightElement.length];
				System.arraycopy(leftElement, 0, elem, 0, leftElement.length);
				System.arraycopy(rightElement, 0, elem, leftElement.length, rightElement.length);
				result.add(elem);
			}
		}
		return result;
	}
}
