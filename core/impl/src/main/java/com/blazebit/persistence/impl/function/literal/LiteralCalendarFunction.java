/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.literal;

/**
 *
 * @author Christian Beikov
 * @since 1.6.0
 */
public class LiteralCalendarFunction extends LiteralFunction {

    public static final String FUNCTION_NAME = "LITERAL_CALENDAR";

    @Override
    public Class<?> getReturnType(Class<?> firstArgumentType) {
        return java.util.Calendar.class;
    }

    @Override
    protected String getFunctionName() {
        return FUNCTION_NAME;
    }

}
