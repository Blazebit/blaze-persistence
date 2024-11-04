/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.day;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class DB2TruncDayFunction extends TruncDayFunction {

    public DB2TruncDayFunction() {
        super("DATE_TRUNC('day', ?1)");
    }

}
