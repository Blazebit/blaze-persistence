/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.day;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class H2DayAddFunction extends DayAddFunction {

    public H2DayAddFunction() {
        super("DATEADD(day, ?2, ?1)");
    }

}
