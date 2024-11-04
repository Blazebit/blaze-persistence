/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.second;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class H2SecondAddFunction extends SecondAddFunction {

    public H2SecondAddFunction() {
        super("DATEADD(second, ?2, ?1)");
    }

}
