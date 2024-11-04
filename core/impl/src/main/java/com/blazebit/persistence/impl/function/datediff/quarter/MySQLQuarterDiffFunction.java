/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.quarter;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MySQLQuarterDiffFunction extends QuarterDiffFunction {

    public MySQLQuarterDiffFunction() {
        super("timestampdiff(QUARTER, ?1, ?2)");
    }

}
