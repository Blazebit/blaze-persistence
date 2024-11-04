/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.day;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class SqliteDayFunction extends DayFunction {

    public SqliteDayFunction() {
        super("cast(strftime('%d',?1) as integer)");
    }
}
