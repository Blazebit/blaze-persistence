/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.isoweek;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class SqliteIsoWeekFunction extends IsoWeekFunction {

    public SqliteIsoWeekFunction() {
        super("(cast(strftime('%j', date(?1, '-3 days', 'weekday 4')) as integer) - 1) / 7 + 1");
    }
}
