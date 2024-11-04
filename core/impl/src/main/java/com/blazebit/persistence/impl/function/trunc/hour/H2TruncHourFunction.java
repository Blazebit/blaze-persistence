/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.hour;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class H2TruncHourFunction extends TruncHourFunction {

    public H2TruncHourFunction() {
        super("DATEADD(HOUR, DATEDIFF(HOUR, '1900-01-01', ?1), '1900-01-01')");
    }

}
