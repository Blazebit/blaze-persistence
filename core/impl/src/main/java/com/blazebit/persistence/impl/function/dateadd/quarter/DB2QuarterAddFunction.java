/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.quarter;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class DB2QuarterAddFunction extends QuarterAddFunction {

    public DB2QuarterAddFunction() {
        super("?1 + (?2 * 3) MONTH");
    }

}
