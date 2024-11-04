/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.year;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class PostgreSQLTruncYearFunction extends TruncYearFunction {

    public PostgreSQLTruncYearFunction() {
        super("DATE_TRUNC('year', ?1)");
    }

}
