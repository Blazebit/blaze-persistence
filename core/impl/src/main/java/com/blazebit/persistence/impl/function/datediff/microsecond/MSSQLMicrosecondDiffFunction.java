/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.microsecond;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MSSQLMicrosecondDiffFunction extends MicrosecondDiffFunction {

    public MSSQLMicrosecondDiffFunction() {
        super("DATEDIFF_BIG(mcs, ?1, ?2)");
    }
}
