/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.month;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class DB2MonthFunction extends MonthFunction {

    public DB2MonthFunction() {
        super("month(?1)");
    }
}
