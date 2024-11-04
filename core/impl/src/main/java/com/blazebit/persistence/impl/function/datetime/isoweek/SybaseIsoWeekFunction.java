/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.isoweek;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class SybaseIsoWeekFunction extends IsoWeekFunction {

    public SybaseIsoWeekFunction() {
        super("datepart(cwk, ?1)");
    }
}
