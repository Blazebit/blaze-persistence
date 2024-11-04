/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.day;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MSSQLTruncDayFunction extends TruncDayFunction {

    public MSSQLTruncDayFunction() {
        super("DATEADD(DAY, DATEDIFF(DAY, 0, ?1), 0)");
    }

}
