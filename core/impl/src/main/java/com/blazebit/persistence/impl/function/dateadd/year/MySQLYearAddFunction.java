/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.year;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MySQLYearAddFunction extends YearAddFunction {

    public MySQLYearAddFunction() {
        super("DATE_ADD(?1, INTERVAL ?2 YEAR)");
    }

}
