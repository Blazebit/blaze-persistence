/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.epochday;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class DefaultEpochDayFunction extends EpochDayFunction {
    public DefaultEpochDayFunction() {
        super("datediff(s, '1970-01-01 00:00:00', ?1) / " + (24 * 60 * 60));
    }
}
