/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.second;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MySQLTruncSecondFunction extends TruncSecondFunction {

    public MySQLTruncSecondFunction() {
        super("date_add('1900-01-01', interval TIMESTAMPDIFF(SECOND, '1900-01-01', ?1) SECOND)");
    }

}
