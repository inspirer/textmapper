package net.sf.lapg.templates.model.xml;

import net.sf.lapg.templates.api.ILocatedEntity;

public abstract class XmlElement implements ILocatedEntity {

	public abstract void toString(StringBuffer sb);

	@Override
	public final String toString() {
		StringBuffer sb = new StringBuffer();
		toString(sb);
		return sb.toString();
	}

	public String getLocation() {
		return null;
	}
}
