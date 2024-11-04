/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.year;

import com.blazebit.persistence.impl.function.trunc.TruncFunction;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public abstract class TruncYearFunction extends TruncFunction {

    public static final String NAME = "TRUNC_YEAR";

    public TruncYearFunction(String template) {
        super(NAME, template);
    }

}
