/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.microseconds;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class DB2TruncMicrosecondsFunction extends TruncMicrosecondsFunction {

    public DB2TruncMicrosecondsFunction() {
        super("DATE_TRUNC('microseconds', ?1)");
    }

}
