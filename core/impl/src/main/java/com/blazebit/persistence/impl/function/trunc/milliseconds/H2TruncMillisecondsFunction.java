/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.milliseconds;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class H2TruncMillisecondsFunction extends TruncMillisecondsFunction {

    public H2TruncMillisecondsFunction() {
        super("DATEADD(MILLISECOND, DATEDIFF(MILLISECOND, '1900-01-01', ?1), '1900-01-01')");
    }

}
