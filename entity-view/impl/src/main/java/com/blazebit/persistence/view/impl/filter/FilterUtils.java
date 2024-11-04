/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.view.impl.filter;

import java.io.Serializable;
import java.text.ParseException;

import com.blazebit.text.FormatUtils;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public final class FilterUtils {

    private FilterUtils() {
    }

    @SuppressWarnings("unchecked")
    public static Object parseValue(Class<?> clazz, Object value) {
        try {
            return FormatUtils.getParsedValue((Class<? extends Serializable>) clazz, value.toString());
        } catch (ParseException ex) {
            throw new IllegalArgumentException("The given value '" + value + "' could not be parsed into an object of type '" + clazz.getName() + "'", ex);
        }
    }
}
