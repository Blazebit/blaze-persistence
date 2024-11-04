/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.month;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class DB2TruncMonthFunction extends TruncMonthFunction {

    public DB2TruncMonthFunction() {
        super("DATE_TRUNC('month', ?1)");
    }

}
