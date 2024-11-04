/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.microseconds;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MSSQLTruncMicrosecondsFunction extends TruncMicrosecondsFunction {

    public MSSQLTruncMicrosecondsFunction() {
        super("DATEADD(mcs, -DATEPART(mcs, convert(DATETIME2, ?1)), convert(DATETIME2, ?1))");
    }

}
