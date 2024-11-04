/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.week;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MSSQLWeekInYearFunction extends WeekInYearFunction {

    public MSSQLWeekInYearFunction() {
        super("floor((6 + datepart(dy, convert(date, ?1))) / 7)");
    }
}
