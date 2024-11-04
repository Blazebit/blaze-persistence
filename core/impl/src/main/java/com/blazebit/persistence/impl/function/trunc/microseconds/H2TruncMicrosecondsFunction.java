/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.microseconds;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class H2TruncMicrosecondsFunction extends TruncMicrosecondsFunction {

    public H2TruncMicrosecondsFunction() {
        super("DATEADD(MICROSECOND, DATEDIFF(MICROSECOND, '1900-01-01', ?1), '1900-01-01')");
    }

}
