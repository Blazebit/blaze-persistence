/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.window.every;

import com.blazebit.persistence.spi.DbmsDialect;

/**
 * @author Jan-Willem Gmelig Meyling
 * @author Sayra Ranjha
 * @since 1.4.0
 */
public class FallbackWindowEveryFunction extends WindowEveryFunction {

    public FallbackWindowEveryFunction(DbmsDialect dbmsDialect) {
        super("MIN", dbmsDialect);
    }

}
