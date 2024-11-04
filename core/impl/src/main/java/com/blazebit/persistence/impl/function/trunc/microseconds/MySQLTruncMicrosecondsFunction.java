/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.microseconds;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MySQLTruncMicrosecondsFunction extends TruncMicrosecondsFunction {

    public MySQLTruncMicrosecondsFunction() {
        super("date_add('1900-01-01', interval TIMESTAMPDIFF(MICROSECOND, '1900-01-01', ?1) MICROSECOND)");
    }

}
