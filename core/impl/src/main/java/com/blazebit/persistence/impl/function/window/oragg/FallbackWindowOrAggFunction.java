/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.window.oragg;

import com.blazebit.persistence.spi.DbmsDialect;

/**
 * @author Jan-Willem Gmelig Meyling
 * @author Sayra Ranjha
 * @since 1.4.0
 */
public class FallbackWindowOrAggFunction extends WindowOrAggFunction {

    public FallbackWindowOrAggFunction(DbmsDialect dbmsDialect) {
        super("MAX", dbmsDialect);
    }

}
