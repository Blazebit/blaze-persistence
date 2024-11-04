/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.day;

import com.blazebit.persistence.impl.function.datediff.DateDiffFunction;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class DayDiffFunction extends DateDiffFunction {

    public DayDiffFunction(String template) {
        super("day_diff", template);
    }
}
