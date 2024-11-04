/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.isoweek;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class PostgreSQLIsoWeekFunction extends IsoWeekFunction {

    public PostgreSQLIsoWeekFunction() {
        super("cast(extract(week from ?1) as int)");
    }

}
