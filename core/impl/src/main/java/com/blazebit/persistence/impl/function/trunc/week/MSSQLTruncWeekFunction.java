/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.week;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MSSQLTruncWeekFunction extends TruncWeekFunction {

    public MSSQLTruncWeekFunction() {
        // Implementation from https://stackoverflow.com/a/32955740/2104280
        super("DATEADD(WEEK, DATEDIFF(WEEK, 0, ?1), 0)");
    }

}
