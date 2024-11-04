/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.epochday;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class DB2EpochDayFunction extends EpochDayFunction {

    public DB2EpochDayFunction() {
        super("DAYS(cast(?1 as timestamp))-DAYS('1970-01-01')");
    }
}
