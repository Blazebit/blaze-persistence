/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.week;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class PostgreSQLWeekDiffFunction extends WeekDiffFunction {

    public PostgreSQLWeekDiffFunction() {
        super("-cast(trunc(date_part('day', cast(?1 as timestamp) - cast(?2 as timestamp)) / 7) as integer)");
    }

}