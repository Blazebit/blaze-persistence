/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.milliseconds;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class PostgreSQLMillisecondsAddFunction extends MillisecondsAddFunction {

    public PostgreSQLMillisecondsAddFunction() {
        super("?1 + ?2 * INTERVAL '1 MILLISECONDS'");
    }

}
