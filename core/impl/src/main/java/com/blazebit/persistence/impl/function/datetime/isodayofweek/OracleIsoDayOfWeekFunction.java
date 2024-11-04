/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.isodayofweek;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class OracleIsoDayOfWeekFunction extends IsoDayOfWeekFunction {

    public OracleIsoDayOfWeekFunction() {
        super("(1 + trunc(?1) - trunc(?1, 'IW'))");
    }
}
