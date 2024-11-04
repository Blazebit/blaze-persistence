/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.week;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MySQLTruncWeekFunction extends TruncWeekFunction {

    public MySQLTruncWeekFunction() {
        // Implementation from https://stackoverflow.com/a/32955740/2104280
        super("date_add('1900-01-01', interval TIMESTAMPDIFF(WEEK, '1900-01-01', ?1) WEEK)");
    }

}
