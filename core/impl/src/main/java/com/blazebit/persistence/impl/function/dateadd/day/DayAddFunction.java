/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.day;

import com.blazebit.persistence.impl.function.dateadd.DateAddFunction;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class DayAddFunction extends DateAddFunction {

    public static final String NAME = "ADD_DAY";

    public DayAddFunction() {
        this("DATE_ADD(?1, INTERVAL ?2 DAY)");
    }

    public DayAddFunction(String template) {
        super(NAME, template);
    }

}
