/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.microsecond;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class OracleMicrosecondFunction extends MicrosecondFunction {

    public OracleMicrosecondFunction() {
        super("to_number(to_char(cast(?1 as timestamp),'FF6'))");
    }
}
