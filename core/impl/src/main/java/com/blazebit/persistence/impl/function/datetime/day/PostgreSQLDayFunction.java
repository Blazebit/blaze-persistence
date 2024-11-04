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
public class PostgreSQLDayFunction extends DayFunction {

    public PostgreSQLDayFunction() {
        super("cast(extract(day from ?1) as int)");
    }
}
