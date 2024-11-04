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
public class DB2DayOfWeekFunction extends DayOfYearFunction {

    public DB2DayOfWeekFunction() {
        super("dayofweek(?1)");
    }
}
