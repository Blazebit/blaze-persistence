/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.minute;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class PostgreSQLMinuteFunction extends MinuteFunction {

    public PostgreSQLMinuteFunction() {
        super("cast(extract(minute from ?1) as int)");
    }
}
