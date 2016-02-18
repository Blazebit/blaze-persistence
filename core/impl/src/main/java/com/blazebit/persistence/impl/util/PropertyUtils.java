package com.blazebit.persistence.impl.util;

import java.util.Map;

public class PropertyUtils {

	public static boolean getAsBooleanProperty(Map<String, String> properties, String key, boolean defaultValue) {
		String value = properties.get(key);
		return value == null ? defaultValue : Boolean.parseBoolean(value);
	}
}
