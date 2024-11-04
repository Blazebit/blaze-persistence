/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.minute;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class DB2TruncMinuteFunction extends TruncMinuteFunction {

    public DB2TruncMinuteFunction() {
        super("DATE_TRUNC('minute', ?1)");
    }

}
