/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.epochmilli;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public class DB2EpochMillisecondFunction extends EpochMillisecondFunction {

    public DB2EpochMillisecondFunction() {
        super("(select cast(DAYS(cast(t1 as timestamp))-DAYS('1970-01-01') as bigint) * " + (24L * 60L * 60L * 1000L) + " + MIDNIGHT_SECONDS(cast(t1 as timestamp)) * 1000 + MICROSECOND(t1) / 1000 from lateral(values (cast(?1 as timestamp))) as temp(t1))");
    }
}
