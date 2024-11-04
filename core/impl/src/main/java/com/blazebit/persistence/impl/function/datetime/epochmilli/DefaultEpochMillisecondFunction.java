/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.epochmilli;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public class DefaultEpochMillisecondFunction extends EpochMillisecondFunction {
    public DefaultEpochMillisecondFunction() {
        super("datediff(ms, '1970-01-01 00:00:00', ?1)");
    }
}
