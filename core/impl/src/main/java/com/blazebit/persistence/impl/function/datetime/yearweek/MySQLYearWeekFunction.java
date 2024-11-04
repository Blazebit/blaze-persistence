/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.yearweek;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MySQLYearWeekFunction extends YearWeekFunction {

    public MySQLYearWeekFunction() {
        super("DATE_FORMAT(?1, '%x-%v')");
//         Alternatively:
//        super("INSERT(YEARWEEK(?1), 5, 0, '-')");
    }

}
