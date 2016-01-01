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
package org.textmapper.lapg.builder;

import org.textmapper.lapg.api.DerivedSourceElement;
import org.textmapper.lapg.api.SourceElement;
import org.textmapper.lapg.api.Symbol;
import org.textmapper.lapg.api.TemplateParameter;
import org.textmapper.lapg.api.rule.RhsArgument;
import org.textmapper.lapg.api.rule.RhsMapping;
import org.textmapper.lapg.api.rule.RhsSymbol;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

class LiRhsSymbol extends LiRhsPart implements RhsSymbol, TemplatedSymbolRef, DerivedSourceElement {

	private Symbol target;
	// - OR -
	private TemplateParameter parameter;

	private LiRhsArgument[] args;
	private boolean fwdAll;
	private LiRhsMapping mapping;

	LiRhsSymbol(Symbol target, LiRhsArgument[] args, boolean fwdAll, SourceElement origin) {
		super(origin);
		if (target == null) throw new NullPointerException("target");
		this.target = target;
		this.args = args;
		this.fwdAll = fwdAll;
		this.parameter = null;
	}

	LiRhsSymbol(TemplateParameter target, LiRhsArgument[] args, SourceElement origin) {
		super(origin);
		if (target == null) throw new NullPointerException("target");
		this.target = null;
		this.args = args;
		this.fwdAll = false;
		this.parameter = target;
	}

	@Override
	public Symbol getTarget() {
		return target;
	}

	@Override
	public TemplateParameter getTemplateTarget() {
		return parameter;
	}

	@Override
	public RhsArgument[] getArgs() {
		return args;
	}

	@Override
	public boolean isFwdAll() {
		return fwdAll;
	}

	@Override
	public RhsMapping getMapping() {
		return mapping;
	}

	void setMapping(LiRhsMapping mapping) {
		this.mapping = mapping;
	}

	@Override
	List<RhsSymbol[]> expand(ExpansionContext context) {
		// TODO inline?
		return Collections.singletonList(new RhsSymbol[]{this});
	}

	@Override
	public boolean structurallyEquals(LiRhsPart o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LiRhsSymbol that = (LiRhsSymbol) o;

		if (parameter != null ? !parameter.equals(that.parameter) : that.parameter != null) return false;
		if (target != null ? !target.equals(that.target) : that.target != null) return false;
		if (!Arrays.equals(args, that.args)) return false;

		return true;
	}

	@Override
	public int structuralHashCode() {
		int result = target != null ? target.hashCode() : 0;
		result = 31 * result + (parameter != null ? parameter.hashCode() : 0);
		result = 31 * result + Arrays.hashCode(args);
		return result;
	}

	@Override
	public Kind getKind() {
		return Kind.Symbol;
	}

	@Override
	protected void toString(StringBuilder sb) {
		boolean isHint = (parameter == null && target.getName() == null);
		if (isHint) {
			sb.append("#");
		}
		sb.append(LiUtil.getSymbolName(this));
		LiUtil.appendArguments(sb, args);
	}

	@Override
	public void setResolvedSymbol(Symbol symbol) {
		this.args = null;
		this.target = symbol;
		this.parameter = null;
	}
}
