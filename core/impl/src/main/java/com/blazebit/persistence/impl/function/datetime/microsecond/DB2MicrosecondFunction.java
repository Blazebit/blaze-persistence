/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.microsecond;

/**
 *
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class DB2MicrosecondFunction extends MicrosecondFunction {

    public DB2MicrosecondFunction() {
        super("MICROSECOND(?1)");
    }
}
