/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.millisecond;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class DB2MillisecondFunction extends MillisecondFunction {

    public DB2MillisecondFunction() {
        super("(MICROSECOND(?1) / 1000)");
    }
}
