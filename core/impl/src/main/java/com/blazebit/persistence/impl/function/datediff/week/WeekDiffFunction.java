/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.week;

import com.blazebit.persistence.impl.function.datediff.DateDiffFunction;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public abstract class WeekDiffFunction extends DateDiffFunction {

    public WeekDiffFunction(String template) {
        super("week_diff", template);
    }
}
