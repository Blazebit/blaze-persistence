/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.day;

/**
 *
 * @author Christian Beikov
 * @since 1.2.0
 */
public class OracleDayDiffFunction extends DayDiffFunction {

    public OracleDayDiffFunction() {
        super("trunc(-(cast(?1 as date) - cast(?2 as date)))");
    }
}