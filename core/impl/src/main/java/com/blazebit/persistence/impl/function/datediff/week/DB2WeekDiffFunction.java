/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.week;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class DB2WeekDiffFunction extends WeekDiffFunction {

    public DB2WeekDiffFunction() {
        super("-int((days(cast(?1 as timestamp)) - days(cast(?2 as timestamp))) / 7)");
    }

}
