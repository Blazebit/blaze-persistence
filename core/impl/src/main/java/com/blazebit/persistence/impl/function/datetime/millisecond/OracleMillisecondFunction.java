/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.millisecond;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class OracleMillisecondFunction extends MillisecondFunction {

    public OracleMillisecondFunction() {
        super("to_number(to_char(cast(?1 as timestamp),'FF3'))");
    }
}
