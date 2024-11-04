/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.dayofyear;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class SqliteDayOfYearFunction extends DayOfYearFunction {

    public SqliteDayOfYearFunction() {
        super("cast(strftime('%j',?1) as integer)");
    }
}
