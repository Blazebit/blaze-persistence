/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.yearofweek;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MSSQLYearOfWeekFunction extends YearOfWeekFunction {

    public MSSQLYearOfWeekFunction() {
        super("datepart(yy, DATEADD(WEEK, DATEDIFF(WEEK, 0, ?1), 0))");
    }

}
