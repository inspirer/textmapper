/**
 * Copyright 2002-2016 Evgeny Gryaznov
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
package org.textmapper.lapg.lalr;

import org.textmapper.lapg.api.Marker;

import java.util.Arrays;
import java.util.Set;

public class MarkerImpl implements Marker {

	private final String name;
	private final int[] states;

	public MarkerImpl(String name, Set<Integer> states) {
		this.name = name;
		this.states = new int[states.size()];
		int i = 0;
		for (Integer s : states) {
			this.states[i++] = s;
		}
		Arrays.sort(this.states);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int[] getStates() {
		return states;
	}
}
