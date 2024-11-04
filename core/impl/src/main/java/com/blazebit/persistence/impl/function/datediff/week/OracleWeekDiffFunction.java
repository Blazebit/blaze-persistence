/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.week;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class OracleWeekDiffFunction extends WeekDiffFunction {

    public OracleWeekDiffFunction() {
        super("trunc(-(cast(?1 as date) - cast(?2 as date))/7)");
    }
}