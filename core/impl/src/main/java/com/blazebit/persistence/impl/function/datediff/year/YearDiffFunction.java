/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.year;

import com.blazebit.persistence.impl.function.datediff.DateDiffFunction;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public abstract class YearDiffFunction extends DateDiffFunction {

    public YearDiffFunction(String template) {
        super("year_diff", template);
    }
}