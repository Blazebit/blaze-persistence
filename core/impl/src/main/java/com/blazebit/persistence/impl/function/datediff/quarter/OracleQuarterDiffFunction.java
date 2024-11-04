/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.quarter;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class OracleQuarterDiffFunction extends QuarterDiffFunction {

    public OracleQuarterDiffFunction() {
        super("trunc(-months_between(?1, ?2)/3)");
    }
}