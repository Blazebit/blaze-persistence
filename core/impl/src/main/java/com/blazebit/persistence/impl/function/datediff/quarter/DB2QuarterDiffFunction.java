/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.quarter;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class DB2QuarterDiffFunction extends QuarterDiffFunction {

    public DB2QuarterDiffFunction() {
        super("-int(months_between(cast(?1 as timestamp),cast(?2 as timestamp))/3)");
    }

}
