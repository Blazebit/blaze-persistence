/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.minute;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class PostgreSQLMinuteAddFunction extends MinuteAddFunction {

    public PostgreSQLMinuteAddFunction() {
        super("?1 + ?2 * INTERVAL '1 MINUTE'");
    }

}
