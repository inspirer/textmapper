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
package org.textmapper.templates.types.ast;

import org.textmapper.templates.types.TypesTree.TextSource;

public class AstLiteralExpression extends AstNode implements IAstExpression {

	private String scon;
	private Integer icon;
	private Boolean bcon;

	public AstLiteralExpression(String scon, Integer icon, Boolean bcon, TextSource input, int start, int end) {
		super(input, start, end);
		this.scon = scon;
		this.icon = icon;
		this.bcon = bcon;
	}

	public String getScon() {
		return scon;
	}
	public Integer getIcon() {
		return icon;
	}
	public Boolean getBcon() {
		return bcon;
	}
	public void accept(AstVisitor v) {
		if (!v.visit(this)) {
			return;
		}

		// TODO for scon
		// TODO for icon
		// TODO for bcon
	}
}
