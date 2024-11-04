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
public class SybaseDayFunction extends DayFunction {

    public SybaseDayFunction() {
        super("datepart(dd, ?1)");
    }
}
