/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.second;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class PostgreSQLSecondAddFunction extends SecondAddFunction {

    public PostgreSQLSecondAddFunction() {
        super("?1 + ?2 * INTERVAL '1 SECOND'");
    }

}
