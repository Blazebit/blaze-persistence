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
public class DB2IsoDayOfWeekFunction extends DayOfYearFunction {

    public DB2IsoDayOfWeekFunction() {
        super("dayofweek_iso(?1)");
    }
}
