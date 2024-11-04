/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.epochmicro;

/**
 * @author Christian Beikov
 * @since 1.4.0
 */
public class DefaultEpochMicrosecondFunction extends EpochMicrosecondFunction {
    public DefaultEpochMicrosecondFunction() {
        super("datediff(mcs, '1970-01-01 00:00:00', ?1)");
    }
}
