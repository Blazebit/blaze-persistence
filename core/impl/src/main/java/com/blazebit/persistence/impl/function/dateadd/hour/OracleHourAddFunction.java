/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.hour;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class OracleHourAddFunction extends HourAddFunction {

    public OracleHourAddFunction() {
        super("cast(?1 as timestamp) + ?2 * INTERVAL '1' HOUR");
    }

}
