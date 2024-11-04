/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.second;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MSSQLTruncSecondFunction extends TruncSecondFunction {

    public MSSQLTruncSecondFunction() {
        super("DATEADD(ms, -DATEPART(ms, convert(datetime, ?1)), convert(datetime, ?1))");
    }

}
