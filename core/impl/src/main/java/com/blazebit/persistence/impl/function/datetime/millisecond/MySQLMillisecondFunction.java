/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.millisecond;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MySQLMillisecondFunction extends MillisecondFunction {

    public MySQLMillisecondFunction() {
        super("round(extract(microsecond from ?1) / 1000, 0)");
    }
}
