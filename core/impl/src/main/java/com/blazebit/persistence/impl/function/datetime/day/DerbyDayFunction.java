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
public class DerbyDayFunction extends DayFunction {

    public DerbyDayFunction() {
        super("day(?1)");
    }
}
