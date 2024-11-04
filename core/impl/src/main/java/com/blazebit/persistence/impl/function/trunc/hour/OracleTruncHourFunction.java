/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.hour;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class OracleTruncHourFunction extends TruncHourFunction {

    public OracleTruncHourFunction() {
        super("TRUNC(?1, 'HH')");
    }

}
