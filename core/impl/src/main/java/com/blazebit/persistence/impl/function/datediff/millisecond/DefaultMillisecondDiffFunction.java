/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.datediff.millisecond;

/**
 * @author Moritz Becker
 * @since 1.2.0
 */
public class DefaultMillisecondDiffFunction extends MillisecondDiffFunction {

    public DefaultMillisecondDiffFunction() {
        super("datediff(ms, ?1, ?2)");
    }
}
