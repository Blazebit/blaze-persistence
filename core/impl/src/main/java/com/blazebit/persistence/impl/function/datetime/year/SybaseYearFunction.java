/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.year;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class SybaseYearFunction extends YearFunction {

    public SybaseYearFunction() {
        super("datepart(yy, ?1)");
    }
}
