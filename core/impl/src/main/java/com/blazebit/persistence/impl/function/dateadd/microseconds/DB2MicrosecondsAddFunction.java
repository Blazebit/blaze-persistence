/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.microseconds;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class DB2MicrosecondsAddFunction extends MicrosecondsAddFunction {

    public DB2MicrosecondsAddFunction() {
        super("cast(?1 as timestamp) + (?2) MICROSECONDS");
    }

}
