/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.quarter;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class DefaultQuarterDiffFunction extends QuarterDiffFunction {

    public DefaultQuarterDiffFunction() {
        super("datediff(q, ?1, ?2)");
    }

}
