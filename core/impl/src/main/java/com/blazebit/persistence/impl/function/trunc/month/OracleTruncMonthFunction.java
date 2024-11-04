/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.month;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class OracleTruncMonthFunction extends TruncMonthFunction {

    public OracleTruncMonthFunction() {
        super("TRUNC(?1, 'MM')");
    }

}
