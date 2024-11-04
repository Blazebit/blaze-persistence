/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.milliseconds;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MySQLTruncMillisecondsFunction extends TruncMillisecondsFunction {

    public MySQLTruncMillisecondsFunction() {
        // MySQL does not support MILLISECOND for DATE_ADD and TIMESTAMPDIFF
        super("date_add('1900-01-01', interval (ROUND(TIMESTAMPDIFF(MICROSECOND, '1900-01-01', ?1), -3)) MICROSECOND)");
    }

}
