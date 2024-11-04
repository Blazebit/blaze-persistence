/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.dayofweek;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class PostgreSQLDayOfWeekFunction extends DayOfWeekFunction {

    public PostgreSQLDayOfWeekFunction() {
        super("cast(extract(dow from ?1) as int) + 1");
    }
}
