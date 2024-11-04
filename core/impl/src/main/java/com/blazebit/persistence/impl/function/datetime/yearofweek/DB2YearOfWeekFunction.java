/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.yearofweek;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class DB2YearOfWeekFunction extends YearOfWeekFunction {

    public DB2YearOfWeekFunction() {
        super("YEAR(DATE_TRUNC('week', ?1))");
    }

}
