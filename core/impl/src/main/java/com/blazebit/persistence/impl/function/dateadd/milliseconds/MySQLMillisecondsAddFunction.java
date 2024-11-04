/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.milliseconds;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MySQLMillisecondsAddFunction extends MillisecondsAddFunction {

    public MySQLMillisecondsAddFunction() {
        super("DATE_ADD(?1, INTERVAL ?2 * 1000 MICROSECOND)");
    }

}
