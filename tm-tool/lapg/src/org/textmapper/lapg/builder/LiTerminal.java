/**
 * Copyright 2002-2013 Evgeny Gryaznov
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
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.Terminal;

/**
 * evgeny, 11/16/12
 */
public class LiTerminal extends LiSymbol implements Terminal {

	private Symbol softClass;

	public LiTerminal(String name, String type, SourceElement origin) {
		super(name, type, origin);
	}

	@Override
	public boolean isTerm() {
		return true;
	}

	@Override
	public Symbol getSoftClass() {
		return softClass;
	}

	void setSoftClass(Terminal sc) {
		assert softClass == null;
		assert getType() == null;
		assert !sc.isSoft();

		softClass = sc;
	}

	@Override
	public String getType() {
		if (softClass != null) {
			return softClass.getType();
		}
		return super.getType();
	}

	@Override
	public boolean isSoft() {
		return softClass != null;
	}

	@Override
	public boolean isConstant() {
		// TODO fixme, detect constant terminals
		return getType() == null;
	}
}

