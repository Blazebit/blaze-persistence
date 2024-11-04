/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.quarter;

import com.blazebit.persistence.impl.function.datediff.DateDiffFunction;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public abstract class QuarterDiffFunction extends DateDiffFunction {

    public QuarterDiffFunction(String template) {
        super("quarter_diff", template);
    }
}