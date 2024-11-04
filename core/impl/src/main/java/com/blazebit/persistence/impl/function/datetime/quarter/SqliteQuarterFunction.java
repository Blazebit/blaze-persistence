/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.quarter;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class SqliteQuarterFunction extends QuarterFunction {

    public SqliteQuarterFunction() {
        super("(cast(strftime('%m', ?1) as integer) + 2) / 3");
    }
}
