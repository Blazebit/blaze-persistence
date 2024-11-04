/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.week;

import com.blazebit.persistence.impl.function.dateadd.DateAddFunction;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class WeekAddFunction extends DateAddFunction {

    public static final String NAME = "ADD_WEEK";

    public WeekAddFunction() {
        this("DATE_ADD(?1, INTERVAL ?2 WEEK)");
    }

    public WeekAddFunction(String template) {
        super(NAME, template);
    }

}
