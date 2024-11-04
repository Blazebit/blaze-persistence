/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.millisecond;

/**
 *
 * @author Moritz Becker
 * @since 1.2.0
 */
public class DB2MillisecondDiffFunction extends MillisecondDiffFunction {

    public DB2MillisecondDiffFunction() {
        // NOTE: we need lateral, otherwise the alias will be lost in the subquery
        super("(select cast((days(t2) - days(t1)) as bigint) * " + (24 * 60 * 60 * 1000) + " + (midnight_seconds(t2) - midnight_seconds(t1)) * 1000 + (microsecond(t2) - microsecond(t1)) / 1000 from lateral(values (cast(?1 as timestamp), cast(?2 as timestamp))) as temp(t1,t2))");
    }
}
