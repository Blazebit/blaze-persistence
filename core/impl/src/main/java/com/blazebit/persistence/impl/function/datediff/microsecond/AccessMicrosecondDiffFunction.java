/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.microsecond;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class AccessMicrosecondDiffFunction extends MicrosecondDiffFunction {

    public AccessMicrosecondDiffFunction() {
        super("DateDiff('s', ?1, ?2) * 1000000");
    }
}