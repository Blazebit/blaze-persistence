/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.day;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class OracleTruncDayFunction extends TruncDayFunction {

    public OracleTruncDayFunction() {
        super("TRUNC(?1, 'DD')");
    }

}
