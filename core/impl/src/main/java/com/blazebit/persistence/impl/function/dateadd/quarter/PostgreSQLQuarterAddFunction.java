/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.quarter;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class PostgreSQLQuarterAddFunction extends QuarterAddFunction {

    public PostgreSQLQuarterAddFunction() {
        super("?1 + ?2 * INTERVAL '3 MONTH'");
    }

}
