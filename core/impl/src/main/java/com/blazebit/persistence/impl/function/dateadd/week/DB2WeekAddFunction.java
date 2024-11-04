/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.week;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class DB2WeekAddFunction extends WeekAddFunction {

    public DB2WeekAddFunction() {
        super("?1 + (?2 * 7) DAY");
    }

}
