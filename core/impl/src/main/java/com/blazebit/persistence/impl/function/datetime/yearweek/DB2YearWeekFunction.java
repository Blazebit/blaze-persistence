/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.yearweek;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class DB2YearWeekFunction extends YearWeekFunction {

    public DB2YearWeekFunction() {
        super("VARCHAR_FORMAT(?1, 'IYYY-IW')");
    }

}
