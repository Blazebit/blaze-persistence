/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.quarter;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class H2QuarterDiffFunction extends QuarterDiffFunction {

    public H2QuarterDiffFunction() {
        super("cast((datediff(mm, ?1, ?2) / 3) as bigint)");
    }
}
