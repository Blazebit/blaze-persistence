/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.microsecond;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MySQLMicrosecondFunction extends MicrosecondFunction {

    public MySQLMicrosecondFunction() {
        super("extract(microsecond from ?1)");
    }
}
