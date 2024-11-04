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
public class LiteralDateFunction extends LiteralFunction {

    public static final String FUNCTION_NAME = "LITERAL_DATE";

    @Override
    public Class<?> getReturnType(Class<?> firstArgumentType) {
        return java.sql.Date.class;
    }

    @Override
    protected String getFunctionName() {
        return FUNCTION_NAME;
    }

}
