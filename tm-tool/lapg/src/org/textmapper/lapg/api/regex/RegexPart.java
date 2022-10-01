/**
 * Copyright 2002-2022 Evgeny Gryaznov
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
package org.textmapper.lapg.api.regex;

import org.textmapper.lapg.regex.RegexDefTree.TextSource;

public interface RegexPart {

	/**
	 * @return true if regular expression is constant
	 */
	boolean isConstant();

	String getConstantValue();

	/**
	 * @return expected length, or -1 if length is variable
	 */
	int getLength(RegexContext context);

	<T> T accept(RegexSwitch<T> switch_);

	String getText();

	int getOffset();

	TextSource getSource();
}
