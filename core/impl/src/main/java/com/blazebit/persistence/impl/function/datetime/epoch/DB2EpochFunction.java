/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.epoch;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class DB2EpochFunction extends EpochFunction {

    public DB2EpochFunction() {
        super("(select (DAYS(cast(t1 as timestamp))-DAYS('1970-01-01')) * " + (24 * 60 * 60) + " + MIDNIGHT_SECONDS(cast(t1 as timestamp)) from lateral(values (cast(?1 as timestamp))) as temp(t1))");
    }
}
