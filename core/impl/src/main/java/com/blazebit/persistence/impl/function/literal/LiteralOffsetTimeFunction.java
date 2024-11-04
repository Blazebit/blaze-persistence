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
public class LiteralOffsetTimeFunction extends LiteralFunction {

    public static final String FUNCTION_NAME = "LITERAL_OFFSETTIME";
    private static final Class<?> CLAZZ = loadClass("java.time.OffsetTime");

    @Override
    public Class<?> getReturnType(Class<?> firstArgumentType) {
        return CLAZZ;
    }

    @Override
    protected String getFunctionName() {
        return FUNCTION_NAME;
    }

}
