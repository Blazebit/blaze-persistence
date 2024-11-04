/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.month;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MSSQLTruncMonthFunction extends TruncMonthFunction {

    public MSSQLTruncMonthFunction() {
        super("DATEADD(MONTH, DATEDIFF(MONTH, 0, ?1), 0)");
    }

}
