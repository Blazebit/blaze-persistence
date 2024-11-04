/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.trunc.second;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class H2TruncSecondFunction extends TruncSecondFunction {

    public H2TruncSecondFunction() {
        // Use Milliseconds, as seconds overflow
        super("DATEADD(MILLISECONDS, DATEDIFF(SECOND, '1900-01-01', ?1) * 1000, '1900-01-01')");
    }

}
