/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.millisecond;

import com.blazebit.persistence.impl.function.datediff.DateDiffFunction;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public abstract class MillisecondDiffFunction extends DateDiffFunction {

    public MillisecondDiffFunction(String template) {
        super("millisecond_diff", template);
    }

    @Override
    public Class<?> getReturnType(Class<?> firstArgumentType) {
        return Long.class;
    }
}
