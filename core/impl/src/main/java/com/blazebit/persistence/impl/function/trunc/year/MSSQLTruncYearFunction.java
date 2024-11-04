/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.year;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MSSQLTruncYearFunction extends TruncYearFunction {

    public MSSQLTruncYearFunction() {
        super("DATEADD(YEAR, DATEDIFF(YEAR, 0, ?1), 0)");
    }

}
