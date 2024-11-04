/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.dayofweek;

import com.blazebit.persistence.impl.function.datetime.dayofyear.DayOfYearFunction;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MySQLDayOfWeekFunction extends DayOfYearFunction {

    public MySQLDayOfWeekFunction() {
        super("dayofweek(?1)");
    }
}
