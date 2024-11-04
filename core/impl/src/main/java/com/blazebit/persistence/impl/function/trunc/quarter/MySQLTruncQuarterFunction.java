/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.quarter;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MySQLTruncQuarterFunction extends TruncQuarterFunction {

    public MySQLTruncQuarterFunction() {
        super("date_add('1900-01-01', interval TIMESTAMPDIFF(QUARTER, '1900-01-01', ?1) QUARTER)");
    }

}
