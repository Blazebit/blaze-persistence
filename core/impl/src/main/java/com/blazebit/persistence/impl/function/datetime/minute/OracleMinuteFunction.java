/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.minute;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class OracleMinuteFunction extends MinuteFunction {

    public OracleMinuteFunction() {
        super("extract(minute from cast(?1 as timestamp))");
    }
}
