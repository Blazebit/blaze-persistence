/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.day;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class MSSQLDayFunction extends DayFunction {

    public MSSQLDayFunction() {
        super("datepart(dd, convert(date, ?1))");
    }
}
