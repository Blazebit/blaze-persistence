/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.dayofyear;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class OracleDayOfYearFunction extends DayOfYearFunction {

    public OracleDayOfYearFunction() {
        super("to_number(to_char(?1, 'DDD'))");
    }
}
