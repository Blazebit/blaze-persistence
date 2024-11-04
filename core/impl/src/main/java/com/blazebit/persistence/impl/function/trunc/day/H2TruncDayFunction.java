/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.day;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class H2TruncDayFunction extends TruncDayFunction {

    public H2TruncDayFunction() {
        super("DATEADD(DAY, DATEDIFF(DAY, '1900-01-01', ?1), '1900-01-01')");
    }

}
