/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.minute;

import com.blazebit.persistence.impl.function.datediff.DateDiffFunction;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class MinuteDiffFunction extends DateDiffFunction {

    public MinuteDiffFunction(String template) {
        super("minute_diff", template);
    }
}