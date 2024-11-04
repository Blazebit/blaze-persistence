/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.epochmicro;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public class PostgreSQLEpochMicrosecondFunction extends EpochMicrosecondFunction {

    public PostgreSQLEpochMicrosecondFunction() {
        super("cast(trunc(date_part('epoch', cast(?1 as timestamp) - DATE '1970-01-01') * 1000000) as bigint)");
    }
}
