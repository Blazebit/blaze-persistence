/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.year;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class PostgreSQLYearFunction extends YearFunction {

    public PostgreSQLYearFunction() {
        super("cast(extract(year from ?1) as int)");
    }
}
