/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.quarter;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class OracleQuarterFunction extends QuarterFunction {

    public OracleQuarterFunction() {
        super("to_number(to_char(?1, 'Q'))");
    }
}
