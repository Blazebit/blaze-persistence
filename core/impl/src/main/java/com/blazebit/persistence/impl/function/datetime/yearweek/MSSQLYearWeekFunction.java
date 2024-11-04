/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.yearweek;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MSSQLYearWeekFunction extends YearWeekFunction {

    public MSSQLYearWeekFunction() {
        super("(select CONCAT(datepart(yy, CONVERT(date, DATEADD(WEEK, DATEDIFF(WEEK, 0, x.y), 0))), '-', DATEPART(isowk, CONVERT(date, x.y))) from (values (?1)) x(y))");
    }

}
