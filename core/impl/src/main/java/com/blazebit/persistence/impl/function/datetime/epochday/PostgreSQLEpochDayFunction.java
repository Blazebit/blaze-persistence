/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.epochday;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class PostgreSQLEpochDayFunction extends EpochDayFunction {

    public PostgreSQLEpochDayFunction() {
        super("cast(extract(epoch from ?1) as int) / " + (24 * 60 * 60));
    }
}
