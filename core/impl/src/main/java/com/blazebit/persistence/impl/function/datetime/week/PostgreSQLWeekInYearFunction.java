/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.week;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class PostgreSQLWeekInYearFunction extends WeekInYearFunction {

    public PostgreSQLWeekInYearFunction() {
        super("cast(floor((6 + extract(doy from ?1)) / 7) as int)");
    }
}
