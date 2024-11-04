/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.quarter;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class OracleQuarterAddFunction extends QuarterAddFunction {

    public OracleQuarterAddFunction() {
        super("?1 + ?2 * 3 * INTERVAL '1' MONTH");
    }

}
