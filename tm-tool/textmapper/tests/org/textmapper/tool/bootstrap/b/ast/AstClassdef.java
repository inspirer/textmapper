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
package org.textmapper.tool.bootstrap.b.ast;

import java.util.List;
import org.textmapper.tool.bootstrap.b.SampleBTree.TextSource;

public class AstClassdef extends AstNode implements IAstClassdefNoEoi {

	private final boolean tc;
	private final String ID;
	private final List<AstClassdeflistItem> classdeflist;
	private final boolean te;
	private final String identifier;

	public AstClassdef(boolean tc, String ID, List<AstClassdeflistItem> classdeflist, boolean te, String identifier, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.tc = tc;
		this.ID = ID;
		this.classdeflist = classdeflist;
		this.te = te;
		this.identifier = identifier;
	}

	public boolean isTc() {
		return tc;
	}

	public String getID() {
		return ID;
	}

	public List<AstClassdeflistItem> getClassdeflist() {
		return classdeflist;
	}

	public boolean isTe() {
		return te;
	}

	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void accept(AstVisitor v) {
		if (!v.visit(this)) {
			return;
		}
		if (classdeflist != null) {
			for (AstClassdeflistItem it : classdeflist) {
				it.accept(v);
			}
		}
	}
}
