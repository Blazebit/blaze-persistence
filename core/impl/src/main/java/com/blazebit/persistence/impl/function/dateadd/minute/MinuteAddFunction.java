/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.minute;

import com.blazebit.persistence.impl.function.dateadd.DateAddFunction;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MinuteAddFunction extends DateAddFunction {

    public static final String NAME = "ADD_MINUTE";

    public MinuteAddFunction() {
        this("DATE_ADD(?1, INTERVAL ?2 MINUTE)");
    }

    public MinuteAddFunction(String template) {
        super(NAME, template);
    }

}
