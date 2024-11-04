/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.month;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MySQLTruncMonthFunction extends TruncMonthFunction {

    public MySQLTruncMonthFunction() {
        super("date_add('1900-01-01', interval TIMESTAMPDIFF(MONTH, '1900-01-01', ?1) MONTH)");
    }

}
