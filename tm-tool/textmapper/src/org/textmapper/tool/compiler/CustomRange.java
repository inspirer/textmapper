/**
 * Copyright 2002-2017 Evgeny Gryaznov
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

import org.textmapper.lapg.api.Rule;

public class CustomRange implements Comparable<CustomRange> {
	private Rule rule;
	private int start;
	private int end;
	private RangeType type;

	public CustomRange(Rule rule, int start, int end, RangeType type) {
		this.rule = rule;
		this.start = start;
		this.end = end;
		this.type = type;
	}

	public Rule getRule() {
		return rule;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public RangeType getType() {
		return type;
	}

	@Override
	public int compareTo(CustomRange o) {
		if (this.end < o.end) return -1;
		if (this.end > o.end) return 1;
		if (this.start > o.start) return -1;
		if (this.start < o.start) return 1;
		return 0;
	}
}
