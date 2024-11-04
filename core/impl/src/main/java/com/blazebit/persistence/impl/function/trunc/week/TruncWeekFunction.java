/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.week;

import com.blazebit.persistence.impl.function.trunc.TruncFunction;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class TruncWeekFunction extends TruncFunction {

    public static final String NAME = "TRUNC_WEEK";

    public TruncWeekFunction() {
        this("DATE_TRUNC('week', ?1)");
    }

    public TruncWeekFunction(String template) {
        super(NAME, template);
    }

}
