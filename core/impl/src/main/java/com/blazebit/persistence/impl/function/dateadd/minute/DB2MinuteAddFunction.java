/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.minute;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class DB2MinuteAddFunction extends MinuteAddFunction {

    public DB2MinuteAddFunction() {
        super("cast(?1 as timestamp) + (?2) MINUTE");
    }

}
