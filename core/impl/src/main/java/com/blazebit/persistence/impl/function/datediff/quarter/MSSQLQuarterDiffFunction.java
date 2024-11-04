/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.quarter;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MSSQLQuarterDiffFunction extends QuarterDiffFunction {

    public MSSQLQuarterDiffFunction() {
        super("round(datediff(mm, ?1, ?2)/3,0,1)");
    }

}
