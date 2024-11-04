/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.week;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class PostgreSQLWeekAddFunction extends WeekAddFunction {

    public PostgreSQLWeekAddFunction() {
        super("?1 + ?2 * INTERVAL '1 WEEK'");
    }

}
