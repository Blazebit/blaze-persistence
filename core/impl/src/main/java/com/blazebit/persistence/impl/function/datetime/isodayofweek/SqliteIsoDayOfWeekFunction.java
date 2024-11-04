/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.isodayofweek;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class SqliteIsoDayOfWeekFunction extends IsoDayOfWeekFunction {

    public SqliteIsoDayOfWeekFunction() {
        super("(((cast(strftime('%w',?1) as integer) + 6) % 7) + 1)");
    }
}
