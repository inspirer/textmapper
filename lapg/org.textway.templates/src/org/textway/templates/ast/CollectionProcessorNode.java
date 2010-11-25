/**
 * Copyright 2002-2010 Evgeny Gryaznov
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
package org.textway.templates.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.textway.templates.api.EvaluationContext;
import org.textway.templates.api.EvaluationException;
import org.textway.templates.api.IEvaluationStrategy;
import org.textway.templates.ast.TemplatesTree.TextSource;


public class CollectionProcessorNode extends ExpressionNode {

	static final int COLLECT = 1;
	static final int COLLECTUNIQUE = 2;
	static final int REJECT = 3;
	static final int SELECT = 4;
	static final int FORALL = 5;
	static final int EXISTS = 6;
	static final int SORT = 7;
	static final int GROUPBY = 8;

	private static final String[] INSTR_WORDS = new String[] { null, "collect", "collectUnique", "reject", "select", "forAll", "exists", "sort", "groupBy" };

	private final ExpressionNode selectExpression;
	private final int instruction;
	private final String varName;
	private final ExpressionNode foreachExpr;

	public CollectionProcessorNode(ExpressionNode forExpr, int instruction, String varName, ExpressionNode foreachExpr, TextSource source, int offset, int endoffset) {
		super(source, offset, endoffset);
		this.selectExpression = forExpr;
		this.instruction = instruction;
		this.varName = varName;
		this.foreachExpr = foreachExpr;
	}

	@Override
	public Object evaluate(EvaluationContext context, IEvaluationStrategy env) throws EvaluationException {
		Object select = env.evaluate(selectExpression, context, false);
		Object prevVar = context.getVariable(varName);
		try {
			Iterator<?> it = env.asAdaptable(select).asSequence();
			if(it == null) {
				throw new EvaluationException("`" + selectExpression.toString() + "` should be array or iterable (instead of "+select.getClass().getCanonicalName()+")");
			}

			if(instruction == SELECT || instruction == REJECT || instruction == COLLECT || instruction == COLLECTUNIQUE) {
				Collection<Object> result = instruction == COLLECTUNIQUE ? new LinkedHashSet<Object>() : new ArrayList<Object>();
				while(it.hasNext()) {
					Object curr = it.next();
					context.setVariable(varName, curr);
					Object val = env.evaluate(foreachExpr, context, instruction == COLLECT || instruction == COLLECTUNIQUE || instruction == SELECT);
					if(instruction != COLLECT && instruction != COLLECTUNIQUE) {
						boolean b = env.toBoolean(val) ^ (instruction == REJECT);
						if(b) {
							result.add(curr);
						}
					} else if(val instanceof Iterable<?>) {
						for(Object v : (Iterable<?>) val) {
							if(v!=null) {
								result.add(v);
							}
						}
					} else if(val instanceof Object[]) {
						for(Object v : (Object[])val) {
							if(v!=null) {
								result.add(v);
							}
						}
					} else if(val != null){
						result.add(val);
					}
				}
				return instruction == COLLECTUNIQUE ? new ArrayList<Object>(result) : result;
			} else if(instruction == GROUPBY) {
				List<Object> result = new ArrayList<Object>();
				Map<Object,Integer> keyToIndex = new HashMap<Object, Integer>();
				while(it.hasNext()) {
					Object curr = it.next();
					context.setVariable(varName, curr);
					Object val = env.evaluate(foreachExpr, context, false);
					Integer index = keyToIndex.get(val);
					if(index == null) {
						keyToIndex.put(val, result.size());
						result.add(curr);
					} else {
						Object existing = result.get(index);
						if(existing instanceof GroupList) {
							((GroupList)existing).add(curr);
						} else {
							GroupList l = new GroupList();
							l.add(existing);
							l.add(curr);
							result.set(index, l);
						}
					}
				}
				return result;
			} else if(instruction == SORT) {
				List<Object> result = new ArrayList<Object>();
				final Map<Object, Comparable<Object>> sortKey = new HashMap<Object, Comparable<Object>>();
				while(it.hasNext()) {
					Object curr = it.next();
					context.setVariable(varName, curr);
					Object val = env.evaluate(foreachExpr, context, false);
					if(!(val instanceof Comparable<?>)) {
						throw new EvaluationException("`" + foreachExpr.toString() + "` should implement Comparable (instead of "+val.getClass().getCanonicalName()+")");
					}
					sortKey.put(curr, (Comparable<Object>)val);
					result.add(curr);
				}
				Object[] arr = result.toArray();
				Arrays.sort(arr, new Comparator<Object>() {
					public int compare(Object o1, Object o2) {
						if(o1 == null) {
							return o2 == null ? 0 : -1;
						}
						if(o2 == null) {
							return 1;
						}
						return sortKey.get(o1).compareTo(sortKey.get(o2));
					}
				});
				return arr;
			} else {
				while(it.hasNext()) {
					Object curr = it.next();
					context.setVariable(varName, curr);
					Object val = env.evaluate(foreachExpr, context, false);
					boolean b = env.toBoolean(val);
					if( b && instruction == EXISTS) {
						return true;
					}
					if(!b && instruction == FORALL ) {
						return false;
					}
				}
				return instruction == FORALL;
			}
		} finally {
			context.setVariable(varName, prevVar);
		}
	}

	@Override
	public void toString(StringBuilder sb) {
		selectExpression.toString(sb);
		sb.append(".");
		sb.append(INSTR_WORDS[instruction]);
		sb.append("(");
		sb.append(varName);
		sb.append("|");
		foreachExpr.toString(sb);
		sb.append(")");
	}

	private static class GroupList extends ArrayList<Object> {
	}
}
