/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.epoch;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class MySQLEpochFunction extends EpochFunction {

    public MySQLEpochFunction() {
        super("unix_timestamp(?1)");
    }
}
