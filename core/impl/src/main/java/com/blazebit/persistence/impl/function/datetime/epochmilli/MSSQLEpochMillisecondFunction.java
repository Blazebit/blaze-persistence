/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.epochmilli;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MSSQLEpochMillisecondFunction extends EpochMillisecondFunction {
    public MSSQLEpochMillisecondFunction() {
        super("DATEDIFF_BIG(ms, '1970-01-01 00:00:00', ?1)");
    }
}
