/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.week;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MySQLWeekInYearFunction extends WeekInYearFunction {

    public MySQLWeekInYearFunction() {
        super("floor((6 + dayofyear(?1)) / 7)");
    }
}
