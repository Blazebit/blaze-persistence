/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.hour;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MSSQLTruncHourFunction extends TruncHourFunction {

    public MSSQLTruncHourFunction() {
        super("DATEADD(HOUR, DATEDIFF(HOUR, 0, ?1), 0)");
    }

}
