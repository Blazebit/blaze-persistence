/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.second;

import com.blazebit.persistence.impl.function.dateadd.DateAddFunction;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class SecondAddFunction extends DateAddFunction {

    public static final String NAME = "ADD_SECOND";

    public SecondAddFunction() {
        this("DATE_ADD(?1, INTERVAL ?2 SECOND)");
    }

    public SecondAddFunction(String template) {
        super(NAME, template);
    }

}
