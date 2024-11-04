/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.dayofyear;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class AccessDayOfYearFunction extends DayOfYearFunction {

    public AccessDayOfYearFunction() {
        super("datepart('y', ?1)");
    }
}
