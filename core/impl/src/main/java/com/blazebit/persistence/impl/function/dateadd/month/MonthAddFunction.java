/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.month;

import com.blazebit.persistence.impl.function.dateadd.DateAddFunction;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MonthAddFunction extends DateAddFunction {

    public static final String NAME = "ADD_MONTH";

    public MonthAddFunction() {
        this("DATE_ADD(?1, INTERVAL ?2 MONTH)");
    }

    public MonthAddFunction(String template) {
        super(NAME, template);
    }

}
