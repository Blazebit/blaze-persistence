/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.hour;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class H2HourAddFunction extends HourAddFunction {

    public H2HourAddFunction() {
        super("DATEADD(hour, ?2, ?1)");
    }

}
