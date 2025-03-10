/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.dayofweek;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class AccessDayOfWeekFunction extends DayOfWeekFunction {

    public AccessDayOfWeekFunction() {
        super("Weekday(?1, 1)");
    }
}
