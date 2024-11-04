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
public class PostgreSQLMonthFunction extends MonthFunction {

    public PostgreSQLMonthFunction() {
        super("cast(extract(month from ?1) as int)");
    }
}
