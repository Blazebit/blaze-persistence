/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.year;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class DB2YearAddFunction extends YearAddFunction {

    public DB2YearAddFunction() {
        super("?1 + (?2) YEAR");
    }

}
