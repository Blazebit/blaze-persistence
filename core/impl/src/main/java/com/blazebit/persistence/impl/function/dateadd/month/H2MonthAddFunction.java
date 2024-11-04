/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.month;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class H2MonthAddFunction extends MonthAddFunction {

    public H2MonthAddFunction() {
        super("DATEADD(month, ?2, ?1)");
    }

}
