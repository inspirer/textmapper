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
package org.textway.lapg.parser;

import org.textway.lapg.api.Annotated;
import org.textway.lapg.parser.ast.IAstNode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LiAnnotated extends LiEntity implements Annotated {

	private static final Map<String,Object> EMPTY_ANN = Collections.<String,Object>emptyMap();

	private Map<String,Object> myAnnotations;

	public LiAnnotated(Map<String, Object> myAnnotations, IAstNode node) {
		super(node);
		this.myAnnotations = myAnnotations != null ? myAnnotations : EMPTY_ANN;
	}

	public void addAnnotation(String name, Object value) {
		if(myAnnotations == EMPTY_ANN) {
			myAnnotations = new HashMap<String, Object>();
		}
		myAnnotations.put(name, value);
	}

	public Object getAnnotation(String name) {
		return myAnnotations.get(name);
	}
}
