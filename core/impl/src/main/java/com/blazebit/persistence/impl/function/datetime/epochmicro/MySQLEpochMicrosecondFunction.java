/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.epochmicro;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public class MySQLEpochMicrosecondFunction extends EpochMicrosecondFunction {

    public MySQLEpochMicrosecondFunction() {
        super("TIMESTAMPDIFF(MICROSECOND, '1970-01-01', ?1)");
    }
}
