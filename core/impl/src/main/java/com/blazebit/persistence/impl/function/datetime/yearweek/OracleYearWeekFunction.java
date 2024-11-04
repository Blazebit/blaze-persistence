/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.yearweek;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class OracleYearWeekFunction extends YearWeekFunction {

    public OracleYearWeekFunction() {
        super("TO_CHAR(?1, 'IYYY-IW')");
    }

}
