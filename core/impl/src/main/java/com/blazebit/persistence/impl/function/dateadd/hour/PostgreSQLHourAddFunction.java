/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.hour;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class PostgreSQLHourAddFunction extends HourAddFunction {

    public PostgreSQLHourAddFunction() {
        super("?1 + ?2 * INTERVAL '1 HOUR'");
    }

}
