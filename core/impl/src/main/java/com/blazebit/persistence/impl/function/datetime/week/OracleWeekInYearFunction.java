/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.week;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class OracleWeekInYearFunction extends WeekInYearFunction {

    public OracleWeekInYearFunction() {
        super("floor((6 + to_number(to_char(?1, 'DDD'))) / 7)");
    }
}
