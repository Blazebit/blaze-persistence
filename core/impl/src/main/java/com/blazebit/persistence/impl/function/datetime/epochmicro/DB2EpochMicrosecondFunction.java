/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.epochmicro;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public class DB2EpochMicrosecondFunction extends EpochMicrosecondFunction {

    public DB2EpochMicrosecondFunction() {
        super("(select (cast(DAYS(cast(t1 as timestamp))-DAYS('1970-01-01') as bigint) * " + (24L * 60L * 60L * 1000000L) + " + cast(MIDNIGHT_SECONDS(cast(t1 as timestamp)) as bigint) * 1000000) + MICROSECOND(t1) from lateral(values (cast(?1 as timestamp))) as temp(t1))");
    }
}
