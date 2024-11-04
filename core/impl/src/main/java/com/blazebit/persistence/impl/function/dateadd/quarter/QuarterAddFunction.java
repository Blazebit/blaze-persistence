/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.quarter;

import com.blazebit.persistence.impl.function.dateadd.DateAddFunction;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class QuarterAddFunction extends DateAddFunction {

    public static final String NAME = "ADD_QUARTER";

    public QuarterAddFunction() {
        this("DATE_ADD(?1, INTERVAL ?2 QUARTER)");
    }

    public QuarterAddFunction(String template) {
        super(NAME, template);
    }

}
