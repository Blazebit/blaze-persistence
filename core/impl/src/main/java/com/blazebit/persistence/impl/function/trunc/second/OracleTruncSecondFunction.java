/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.second;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class OracleTruncSecondFunction extends TruncSecondFunction {

    public OracleTruncSecondFunction() {
        // Works as the precision of DATE is to the second (and no fractions of seconds)
        super("cast(?1 as date)");
    }

}
