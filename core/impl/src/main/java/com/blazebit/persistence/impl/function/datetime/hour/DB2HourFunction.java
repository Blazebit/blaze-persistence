/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.hour;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class DB2HourFunction extends HourFunction {

    public DB2HourFunction() {
        super("hour(?1)");
    }
}
