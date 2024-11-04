/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.microseconds;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class OracleMicrosecondsAddFunction extends MicrosecondsAddFunction {

    public OracleMicrosecondsAddFunction() {
        super("cast(?1 as timestamp) + ?2 * INTERVAL '0.000001' SECOND");
    }

}
