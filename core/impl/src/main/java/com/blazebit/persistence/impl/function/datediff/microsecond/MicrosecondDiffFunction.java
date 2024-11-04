/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.microsecond;

import com.blazebit.persistence.impl.function.datediff.DateDiffFunction;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public abstract class MicrosecondDiffFunction extends DateDiffFunction {

    public MicrosecondDiffFunction(String template) {
        super("microsecond_diff", template);
    }

    @Override
    public Class<?> getReturnType(Class<?> firstArgumentType) {
        return Long.class;
    }
}
