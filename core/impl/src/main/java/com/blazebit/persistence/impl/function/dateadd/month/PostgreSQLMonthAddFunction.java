/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.month;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class PostgreSQLMonthAddFunction extends MonthAddFunction {

    public PostgreSQLMonthAddFunction() {
        super("?1 + ?2 * INTERVAL '1 MONTH'");
    }

}
