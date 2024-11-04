/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.hour;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class SqliteHourFunction extends HourFunction {

    public SqliteHourFunction() {
        super("cast(strftime('%H',?1) as integer)");
    }
}
