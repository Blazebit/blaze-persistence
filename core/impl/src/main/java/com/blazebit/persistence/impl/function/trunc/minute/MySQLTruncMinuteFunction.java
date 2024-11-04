/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.minute;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MySQLTruncMinuteFunction extends TruncMinuteFunction {

    public MySQLTruncMinuteFunction() {
        super("date_add('1900-01-01', interval TIMESTAMPDIFF(MINUTE, '1900-01-01', ?1) MINUTE)");
    }

}
