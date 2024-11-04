/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.epochmicro;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MSSQLEpochMicrosecondFunction extends EpochMicrosecondFunction {
    public MSSQLEpochMicrosecondFunction() {
        super("DATEDIFF_BIG(mcs, convert(DATETIME2, '1970-01-01 00:00:00'), convert(DATETIME2, ?1))");
    }
}
