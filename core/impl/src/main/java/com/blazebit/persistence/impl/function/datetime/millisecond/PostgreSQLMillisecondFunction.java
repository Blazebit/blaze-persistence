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
public class PostgreSQLMillisecondFunction extends MillisecondFunction {

    public PostgreSQLMillisecondFunction() {
        super("cast(extract(milliseconds from ?1) as int) % 1000");
    }
}
