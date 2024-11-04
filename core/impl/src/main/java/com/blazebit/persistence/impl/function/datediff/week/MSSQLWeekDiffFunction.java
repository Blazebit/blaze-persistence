/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.week;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MSSQLWeekDiffFunction extends WeekDiffFunction {

    public MSSQLWeekDiffFunction() {
        super("round(datediff(dd, ?1, ?2)/7,0,1)");
    }

}
