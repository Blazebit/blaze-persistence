/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.hour;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class DB2TruncHourFunction extends TruncHourFunction {

    public DB2TruncHourFunction() {
        super("DATE_TRUNC('hour', ?1)");
    }

}
