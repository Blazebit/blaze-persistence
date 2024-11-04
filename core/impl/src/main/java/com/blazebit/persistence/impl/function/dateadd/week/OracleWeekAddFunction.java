/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.week;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class OracleWeekAddFunction extends WeekAddFunction {

    public OracleWeekAddFunction() {
        super("?1 + ?2 * 7 * INTERVAL '1' DAY");
    }

}
