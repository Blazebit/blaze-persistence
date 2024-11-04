/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.microsecond;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class DB2MicrosecondDiffFunction extends MicrosecondDiffFunction {

    public DB2MicrosecondDiffFunction() {
        // NOTE: we need lateral, otherwise the alias will be lost in the subquery
        super("(select cast((days(t2) - days(t1)) as bigint) * (" + (24L * 60L * 60L * 1000000L) + ") + cast((midnight_seconds(t2) - midnight_seconds(t1)) as bigint) * 1000000 + (microsecond(t2) - microsecond(t1)) from lateral(values (cast(?1 as timestamp), cast(?2 as timestamp))) as temp(t1,t2))");
    }
}
