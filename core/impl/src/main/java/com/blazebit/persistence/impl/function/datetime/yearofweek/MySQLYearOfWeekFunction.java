/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.yearofweek;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MySQLYearOfWeekFunction extends YearOfWeekFunction {

    public MySQLYearOfWeekFunction() {
        // Extract year from truncated time, derived from https://stackoverflow.com/questions/5541326/postgresqls-date-trunc-in-mysql
        super("extract(YEAR FROM date_add('1900-01-01', interval TIMESTAMPDIFF(WEEK, '1900-01-01', ?1) WEEK))");
    }

}
