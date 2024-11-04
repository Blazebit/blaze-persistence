/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.milliseconds;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class H2MillisecondsAddFunction extends MillisecondsAddFunction {

    public H2MillisecondsAddFunction() {
        super("DATEADD(milliseconds, ?2, ?1)");
    }

}
