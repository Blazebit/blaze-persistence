/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.milliseconds;

import com.blazebit.persistence.impl.function.dateadd.DateAddFunction;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MillisecondsAddFunction extends DateAddFunction {

    public static final String NAME = "ADD_MILLISECONDS";

    public MillisecondsAddFunction() {
        this("DATE_ADD(?1, INTERVAL ?2 MILLISECONDS)");
    }

    public MillisecondsAddFunction(String template) {
        super(NAME, template);
    }

}
