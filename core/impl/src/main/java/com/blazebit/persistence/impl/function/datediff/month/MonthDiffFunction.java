/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.month;

import com.blazebit.persistence.impl.function.datediff.DateDiffFunction;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class MonthDiffFunction extends DateDiffFunction {

    public MonthDiffFunction(String template) {
        super("month_diff", template);
    }
}