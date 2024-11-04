/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.month;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class MSSQLMonthFunction extends MonthFunction {

    public MSSQLMonthFunction() {
        super("datepart(mm, convert(date, ?1))");
    }
}
