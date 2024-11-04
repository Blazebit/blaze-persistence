/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.dayofyear;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MySQLDayOfYearFunction extends DayOfYearFunction {

    public MySQLDayOfYearFunction() {
        super("dayofyear(?1)");
    }
}
