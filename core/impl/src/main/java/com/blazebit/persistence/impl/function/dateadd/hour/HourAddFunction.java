/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.hour;

import com.blazebit.persistence.impl.function.dateadd.DateAddFunction;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class HourAddFunction extends DateAddFunction {

    public static final String NAME = "ADD_HOUR";

    public HourAddFunction() {
        this("DATE_ADD(?1, INTERVAL ?2 HOUR)");
    }

    public HourAddFunction(String template) {
        super(NAME, template);
    }

}
