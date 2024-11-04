/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.querydsl;

import com.querydsl.jpa.impl.AbstractJPAQuery;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Utility class for setting query hints
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.6.1
 */
class HintsAccessor {

    private static final Field HINTS_FIELD;

    private static final Logger LOG = Logger.getLogger(HintsAccessor.class.getName());

    private static final Method ENTRIES_METHOD = getEntriesMethod();

    static {
        try {
            HINTS_FIELD = AbstractJPAQuery.class.getDeclaredField("hints");
            HINTS_FIELD.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Cannot initialize hints accessor", e);
        }
    }

    private HintsAccessor() {
    }

    static Iterable<Map.Entry<String, Object>> getHints(AbstractJPAQuery<?, ?> query) {
        try {
            final Object hints = HINTS_FIELD.get(query);

            if (hints instanceof Map) {
                return ((Map<String, Object>) hints).entrySet();
            } else if (ENTRIES_METHOD != null) {
                return (Iterable<Map.Entry<String, Object>>) ENTRIES_METHOD.invoke(hints);
            } else {
                return Collections.emptyList();
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static Method getEntriesMethod() {
        try {
            return Class.forName("com.google.common.collect.Multimap").getMethod("entries");
        } catch (Exception e) {
            LOG.fine("Could not load Multimap.entries() accessor for query hints");
            return null;
        }
    }

}
