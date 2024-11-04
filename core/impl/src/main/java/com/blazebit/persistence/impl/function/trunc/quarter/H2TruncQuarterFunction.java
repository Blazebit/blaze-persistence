/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.quarter;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class H2TruncQuarterFunction extends TruncQuarterFunction {

    public H2TruncQuarterFunction() {
        super("DATEADD(QUARTER, DATEDIFF(QUARTER, '1900-01-01', ?1), '1900-01-01')");
    }

}
