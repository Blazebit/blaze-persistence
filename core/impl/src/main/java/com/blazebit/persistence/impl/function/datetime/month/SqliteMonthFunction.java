/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.month;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class SqliteMonthFunction extends MonthFunction {

    public SqliteMonthFunction() {
        super("cast(strftime('%m',?1) as integer)");
    }
}
