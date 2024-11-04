/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.epochmilli;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public class MySQLEpochMillisecondFunction extends EpochMillisecondFunction {

    public MySQLEpochMillisecondFunction() {
        super("TIMESTAMPDIFF(MICROSECOND, '1970-01-01', ?1) / 1000");
    }
}
