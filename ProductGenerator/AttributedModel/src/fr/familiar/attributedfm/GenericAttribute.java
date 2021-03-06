/*
	This file is part of FaMaTS.

    FaMaTS is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FaMaTS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with FaMaTS.  If not, see <http://www.gnu.org/licenses/>.

 */
package fr.familiar.attributedfm;

import fr.familiar.attributedfm.domain.BoundedElement;
import fr.familiar.attributedfm.domain.Domain;

/**
 * 
 */
public class GenericAttribute extends BoundedElement {

	protected Feature feature;

	protected Object value;

	protected Object defaultValue;

	protected Object nullValue;

	public boolean nonTranstalable=false;
	public boolean runTime=false;

	public boolean nonDesicion=false;
	
	public static final int OBJECT_NULL_VALUE = -1;

	public GenericAttribute(String n, Domain d, Object nv, Object dv) {
		domain = d;
		name = n;
		defaultValue = dv;
		nullValue = nv;
		value = null;
		
	}

	public GenericAttribute(String n) {
		name = n;
		defaultValue = 0;
		nullValue = 0;
		value = 0;
	}
	public Object getNullValue() {
		return nullValue;
	}

	public void setNullValue(Object nullValue) {
		this.nullValue = nullValue;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object o) {
		value = o;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(Object o) {
		defaultValue = o;
	}

	public Feature getFeature() {
		return feature;
	}

	public String toString() {
		return getFullName();
	}

	public String getFullName() {
		return feature.getName() + "." + name;
	}

	public Integer getIntegerValue(Object o) {
		Integer res;
		if (o instanceof Integer) {
			res = (Integer) o;
		} else {
			if (o.equals(nullValue)) {
				res = OBJECT_NULL_VALUE;
			} else {
				res = domain.getIntegerValue(o);
			}
		}
		// Integer res = domain.getIntegerValue(o);
		return res;
	}

	public boolean equals(Object o) {
		boolean b = false;
		if (o instanceof GenericAttribute) {
			GenericAttribute aux = (GenericAttribute) o;
			if (aux.getFeature().equals(feature) && aux.getName().equals(name)) {
				b = true;
			}
		}
		return b;
	}

	public boolean hasFixedValue() {
		return (value != null);
	}
}
