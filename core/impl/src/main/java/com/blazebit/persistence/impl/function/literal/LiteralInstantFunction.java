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
public class LiteralInstantFunction extends LiteralFunction {

    public static final String FUNCTION_NAME = "LITERAL_INSTANT";
    private static final Class<?> CLAZZ = loadClass("java.time.Instant");

    @Override
    public Class<?> getReturnType(Class<?> firstArgumentType) {
        return CLAZZ;
    }

    @Override
    protected String getFunctionName() {
        return FUNCTION_NAME;
    }

}
