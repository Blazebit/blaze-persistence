/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.isodayofweek;

import com.blazebit.persistence.impl.function.datetime.dayofyear.DayOfYearFunction;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MySQLIsoDayOfWeekFunction extends DayOfYearFunction {

    public MySQLIsoDayOfWeekFunction() {
        super("weekday(?1) + 1");
    }
}
