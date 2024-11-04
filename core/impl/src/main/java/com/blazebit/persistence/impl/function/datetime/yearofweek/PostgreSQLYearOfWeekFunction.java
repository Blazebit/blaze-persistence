/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.yearofweek;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class PostgreSQLYearOfWeekFunction extends YearOfWeekFunction {

    public PostgreSQLYearOfWeekFunction() {
        super("cast(extract(year from date_trunc('week', ?1)) as int)");
    }

}
