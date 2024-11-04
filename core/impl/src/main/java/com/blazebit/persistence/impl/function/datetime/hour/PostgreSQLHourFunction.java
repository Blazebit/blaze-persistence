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
public class PostgreSQLHourFunction extends HourFunction {

    public PostgreSQLHourFunction() {
        super("cast(extract(hour from ?1) as int)");
    }
}
