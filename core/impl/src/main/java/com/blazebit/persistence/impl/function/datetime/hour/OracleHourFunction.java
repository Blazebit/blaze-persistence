/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.hour;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class OracleHourFunction extends HourFunction {

    public OracleHourFunction() {
        super("extract(hour from cast(?1 as timestamp))");
    }
}
