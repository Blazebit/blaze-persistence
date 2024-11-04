/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.week;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MySQLWeekDiffFunction extends WeekDiffFunction {

    public MySQLWeekDiffFunction() {
        super("timestampdiff(WEEK, ?1, ?2)");
    }

}
