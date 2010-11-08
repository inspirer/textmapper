package org.textway.templates.types;

import java.util.Collection;

import org.textway.templates.api.types.IClass;
import org.textway.templates.api.types.IFeature;

public class TiClass implements IClass {

	private String name;
	private Collection<IClass> _super;
	private Collection<IFeature> features;
	
	public TiClass(String name, Collection<IClass> _super,
			Collection<IFeature> features) {
		this.name = name;
		this._super = _super;
		this.features = features;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Collection<IClass> getExtends() {
		return _super;
	}

	@Override
	public Collection<IFeature> getFeatures() {
		return features;
	}
}
