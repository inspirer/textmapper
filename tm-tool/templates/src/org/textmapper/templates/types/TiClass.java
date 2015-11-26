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
package org.textmapper.templates.types;

import org.textmapper.templates.api.types.IClass;
import org.textmapper.templates.api.types.IFeature;
import org.textmapper.templates.api.types.IMethod;
import org.textmapper.templates.api.types.IType;

import java.util.*;

public class TiClass implements IClass {

	private String name;
	private String package_;
	private Collection<IClass> _super;
	private Collection<IFeature> features;
	private List<IMethod> methods;

	public TiClass(String name, String package_, Collection<IClass> _super,
				   Collection<IFeature> features, List<IMethod> methods) {
		this.name = name;
		this.package_ = package_;
		this._super = _super;
		this.features = features;
		this.methods = methods;
		if (methods != null) {
			for (IMethod method : methods) {
				((TiMethod) method).setDeclaringClass(this);
			}
		}
	}

	public String getName() {
		return name;
	}

	public String getQualifiedName() {
		return package_ + "." + name;
	}

	public Collection<IClass> getExtends() {
		return _super;
	}

	public Collection<IFeature> getFeatures() {
		return features;
	}

	public Collection<IMethod> getMethods() {
		return methods;
	}

	public IFeature getFeature(String name) {
		for (IFeature f : features) {
			if (f.getName().equals(name)) {
				return f;
			}
		}
		LinkedList<IClass> queue = new LinkedList<>(this.getExtends());
		if (queue.isEmpty()) {
			return null;
		}

		Set<IClass> processed = new HashSet<>();
		processed.add(this);
		processed.addAll(queue);
		while (!queue.isEmpty()) {
			IClass next = queue.remove();
			for (IFeature f : next.getFeatures()) {
				if (f.getName().equals(name)) {
					return f;
				}
			}
			for (IClass cl : next.getExtends()) {
				if (!processed.contains(cl)) {
					processed.add(cl);
					queue.add(cl);
				}
			}
		}
		return null;
	}

	public IMethod getMethod(String name) {
		for (IMethod m : methods) {
			if (m.getName().equals(name)) {
				return m;
			}
		}
		LinkedList<IClass> queue = new LinkedList<>(this.getExtends());
		if (queue.isEmpty()) {
			return null;
		}

		Set<IClass> processed = new HashSet<>();
		processed.add(this);
		processed.addAll(queue);
		while (!queue.isEmpty()) {
			IClass next = queue.remove();
			for (IMethod m : next.getMethods()) {
				if (m.getName().equals(name)) {
					return m;
				}
			}
			for (IClass cl : next.getExtends()) {
				if (!processed.contains(cl)) {
					processed.add(cl);
					queue.add(cl);
				}
			}
		}
		return null;
	}

	public boolean isSubtypeOf(String qualifiedName) {
		Set<IClass> validated = new HashSet<>();
		LinkedList<IClass> queue = new LinkedList<>();
		queue.add(this);
		validated.add(this);
		while (!queue.isEmpty()) {
			IClass next = queue.remove();
			if (next.getQualifiedName().equals(qualifiedName)) {
				return true;
			}
			for (IClass cl : next.getExtends()) {
				if (!validated.contains(cl)) {
					validated.add(cl);
					queue.add(cl);
				}
			}
		}
		return false;
	}

	public boolean isSubtypeOf(IType anotherType) {
		if (!(anotherType instanceof IClass)) {
			return false;
		}
		IClass another = (IClass) anotherType;
		String anotherQualified = another.getQualifiedName();

		return isSubtypeOf(anotherQualified);
	}

	@Override
	public String toString() {
		return package_ + "." + name;
	}
}
