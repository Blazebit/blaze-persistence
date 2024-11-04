/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.week;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class OracleTruncWeekFunction extends TruncWeekFunction {

    public OracleTruncWeekFunction() {
        super("TRUNC(?1, 'IW')");
    }

}
