/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.epochday;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class MySQLEpochDayFunction extends EpochDayFunction {

    public MySQLEpochDayFunction() {
        super("unix_timestamp(?1) / " + (24 * 60 * 60));
    }
}
