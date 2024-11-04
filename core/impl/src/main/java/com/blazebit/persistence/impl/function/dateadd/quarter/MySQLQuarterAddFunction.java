/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.quarter;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MySQLQuarterAddFunction extends QuarterAddFunction {

    public MySQLQuarterAddFunction() {
        super("DATE_ADD(?1, INTERVAL ?2 QUARTER)");
    }

}
