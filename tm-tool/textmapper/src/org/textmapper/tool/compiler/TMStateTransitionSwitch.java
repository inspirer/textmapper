/**
 * Copyright 2002-2015 Evgeny Gryaznov
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

import org.textmapper.lapg.api.LexerState;

import java.util.Map;

/**
 * Gryaznov Evgeny, 9/11/12
 */
public class TMStateTransitionSwitch {

	private Map<LexerState, LexerState> stateSwitch;
	private LexerState defaultTransition;

	public TMStateTransitionSwitch(LexerState defaultTransition) {
		this(null, defaultTransition);
	}

	public TMStateTransitionSwitch(Map<LexerState, LexerState> stateSwitch,
								   LexerState defaultTransition) {
		this.stateSwitch = stateSwitch;
		this.defaultTransition = defaultTransition;
		assert stateSwitch != null || defaultTransition != null;
		assert stateSwitch == null || !stateSwitch.isEmpty();
	}

	public Map<LexerState, LexerState> getStateSwitch() {
		return stateSwitch;
	}

	public LexerState getDefaultTransition() {
		return defaultTransition;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		TMStateTransitionSwitch that = (TMStateTransitionSwitch) o;

		if (defaultTransition != null ? !defaultTransition.equals(that.defaultTransition) : that
				.defaultTransition != null)
			return false;
		if (stateSwitch != null ? !stateSwitch.equals(that.stateSwitch) : that.stateSwitch != null)
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		int result = stateSwitch != null ? stateSwitch.hashCode() : 0;
		result = 31 * result + (defaultTransition != null ? defaultTransition.hashCode() : 0);
		return result;
	}
}
