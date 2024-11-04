/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.milliseconds;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class OracleMillisecondsAddFunction extends MillisecondsAddFunction {

    public OracleMillisecondsAddFunction() {
        super("cast(?1 as timestamp) + ?2 * INTERVAL '0.001' SECOND");
    }

}
