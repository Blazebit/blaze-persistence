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
public class PostgreSQLIsoDayOfWeekFunction extends IsoDayOfWeekFunction {

    public PostgreSQLIsoDayOfWeekFunction() {
        super("cast(extract(isodow from ?1) as int)");
    }
}
