/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.epoch;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class PostgreSQLEpochFunction extends EpochFunction {

    public PostgreSQLEpochFunction() {
        super("extract(epoch from ?1)");
    }
}
