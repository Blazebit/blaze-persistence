/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.quarter;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class PostgreSQLQuarterDiffFunction extends QuarterDiffFunction {

    public PostgreSQLQuarterDiffFunction() {
        super("(select -cast(trunc((date_part('year', t1) * 12 + date_part('month', t1))/3) as integer) from (values (age(?1,?2))) as temp(t1))");
    }

}
