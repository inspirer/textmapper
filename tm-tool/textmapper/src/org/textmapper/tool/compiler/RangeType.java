/**
 * Copyright 2002-2019 Evgeny Gryaznov
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

public class RangeType {

	private String name;
	private String kind;
	private String iface;

	public RangeType(String name, String kind, String iface) {
		this.name = name;
		this.kind = kind;
		this.iface = iface;
	}

	public String getName() {
		return name;
	}

	public String getKind() {
		return kind;
	}

	public String getIface() {
		return iface;
	}
}
