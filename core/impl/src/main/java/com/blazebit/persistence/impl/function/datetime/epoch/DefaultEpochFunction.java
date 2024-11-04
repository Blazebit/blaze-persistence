/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.epoch;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class DefaultEpochFunction extends EpochFunction {
    public DefaultEpochFunction() {
        super("datediff(s, '1970-01-01 00:00:00', ?1)");
    }
}
