/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.hour;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class DB2HourAddFunction extends HourAddFunction {

    public DB2HourAddFunction() {
        super("cast(?1 as timestamp) + (?2) HOUR");
    }

}
