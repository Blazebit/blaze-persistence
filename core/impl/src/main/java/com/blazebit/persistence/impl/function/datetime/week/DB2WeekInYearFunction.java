/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.week;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class DB2WeekInYearFunction extends WeekInYearFunction {

    public DB2WeekInYearFunction() {
        super("floor((6 + dayofyear(?1)) / 7)");
    }
}
