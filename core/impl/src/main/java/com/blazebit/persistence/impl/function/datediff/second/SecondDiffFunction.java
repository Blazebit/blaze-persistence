/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.second;

import com.blazebit.persistence.impl.function.datediff.DateDiffFunction;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class SecondDiffFunction extends DateDiffFunction {

    public SecondDiffFunction(String template) {
        super("second_diff", template);
    }

    @Override
    public Class<?> getReturnType(Class<?> firstArgumentType) {
        return Long.class;
    }
}