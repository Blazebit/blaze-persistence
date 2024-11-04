/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.millisecond;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class MSSQLMillisecondDiffFunction extends MillisecondDiffFunction {

    public MSSQLMillisecondDiffFunction() {
        super("(select cast(datediff(ss,t1,t2) as bigint) * 1000 + cast(datepart(ms,t2) as bigint) - cast(datepart(ms,t1) as bigint) from (values (cast(?1 as datetime),cast(?2 as datetime))) as temp(t1,t2))");
    }
}
