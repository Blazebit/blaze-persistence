/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.impl.function.dateadd.microseconds;

import com.blazebit.persistence.impl.function.dateadd.DateAddFunction;

/**
 * @author Jan-Willem Gmelig Meyling
 * @since 1.4.0
 */
public class MicrosecondsAddFunction extends DateAddFunction {

    public static final String NAME = "ADD_MICROSECONDS";

    public MicrosecondsAddFunction() {
        this("DATE_ADD(?1, INTERVAL ?2 MICROSECONDS)");
    }

    public MicrosecondsAddFunction(String template) {
        super(NAME, template);
    }

}
