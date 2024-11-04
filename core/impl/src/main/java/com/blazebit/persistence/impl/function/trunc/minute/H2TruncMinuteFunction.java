/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.minute;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class H2TruncMinuteFunction extends TruncMinuteFunction {

    public H2TruncMinuteFunction() {
        super("DATEADD(MINUTE, DATEDIFF(MINUTE, '1900-01-01', ?1), '1900-01-01')");
    }

}
