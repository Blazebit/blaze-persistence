/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.millisecond;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class PostgreSQLMillisecondDiffFunction extends MillisecondDiffFunction {

    public PostgreSQLMillisecondDiffFunction() {
        super("-cast(trunc(date_part('epoch', cast(?1 as timestamp) - cast(?2 as timestamp)) * 1000) as bigint)");
    }
}
