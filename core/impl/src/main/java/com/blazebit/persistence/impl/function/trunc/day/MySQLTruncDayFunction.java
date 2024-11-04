/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.day;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MySQLTruncDayFunction extends TruncDayFunction {

    public MySQLTruncDayFunction() {
        super("date_add('1900-01-01', interval TIMESTAMPDIFF(DAY, '1900-01-01', ?1) DAY)");
    }

}
