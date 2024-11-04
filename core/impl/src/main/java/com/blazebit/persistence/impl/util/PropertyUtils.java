/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.util;

import java.util.Map;
import java.util.Properties;

/**
 * @author Christian Beikov
 * @since 1.2.0
 */
public class PropertyUtils {

    private PropertyUtils() {
    }

    public static boolean getAsBooleanProperty(Properties properties, String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }

    public static boolean getAsBooleanProperty(Map<String, String> properties, String key, boolean defaultValue) {
        String value = properties.get(key);
        return value == null ? defaultValue : Boolean.parseBoolean(value);
    }
}
