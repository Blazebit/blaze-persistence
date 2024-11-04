/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datetime.second;

/**
 *
 * @author Christian Beikov
 * @since 1.0.0
 */
public class SybaseSecondFunction extends SecondFunction {

    public SybaseSecondFunction() {
        super("datepart(ss, ?1)");
    }
}
