package net.sf.lapg.templates.model.xml;

import net.sf.lapg.templates.api.IEntity;

public abstract class XmlElement implements IEntity {

	public abstract void toString(StringBuffer sb);

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		toString(sb);
		return sb.toString();
	}

	public String getLocation() {
		return null;
	}
}
