/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.epochday;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class OracleEpochDayFunction extends EpochDayFunction {

    public OracleEpochDayFunction() {
        super("extract(day from (cast(?1 as timestamp) - to_date('1970-01-01', 'yyyy-mm-dd')))");
    }

}

