/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.month;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class H2TruncMonthFunction extends TruncMonthFunction {

    public H2TruncMonthFunction() {
        super("DATEADD(MONTH, DATEDIFF(MONTH, '1900-01-01', ?1), '1900-01-01')");
    }

}
